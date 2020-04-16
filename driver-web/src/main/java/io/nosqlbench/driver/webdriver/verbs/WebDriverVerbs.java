package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.CommandTemplate;
import io.nosqlbench.driver.webdriver.WebContext;
import io.nosqlbench.nb.api.errors.BasicError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Inspiration for how to make this work consistently with the exporter logic
 * is found at
 *
 * <a href="https://github.com/SeleniumHQ/selenium-ide/blob/code-export-java/packages/code-export-java-junit/src/command.js">
 *     The JUnit exporter command generator</a>
 */
public class WebDriverVerbs {
    private final static Logger logger = LoggerFactory.getLogger(WebDriverVerbs.class);
    public final static String COMMAND = "command";

    public enum Verb {

        open(Open.class, (m) -> new Open(m.get("url"),m.get("target"))),
        get(Get.class, (m) -> new Get(m.get("target"))),
        mouseOver(MouseOver.class, (m) -> new MouseOver(Bys.get(m.get("target")))),
        mouseOut(MouseOut.class,(m) -> new MouseOut()),
        setWindowSize(SetWindowSize.class,(m) -> new SetWindowSize(m.get("target"))),

//        find_element(FindElement.class, (m) -> new FindElement(m.get("by"))),
//        find_elements(FindElements.class, (m) -> new FindElements(m.get("by"))),
//
//        // navigation
//        back(NavigateBack.class, (m) -> new NavigateBack()),
//        forward(NavigateForward.class, (m) -> new NavigateForward()),
//        refresh(NavigateRefresh.class, (m) -> new NavigateRefresh()),
//        to(NavigateTo.class, (m) -> new NavigateTo(m.get("to"))),
//
//        // options
//        add_cookie(AddCookie.class, (m) -> new AddCookie(CookieJar.cookie(m))),
//        delete_cookie(DeleteCookie.class, (m) -> new DeleteCookie(CookieJar.cookie(m))),
//        delete_all_cookies(DeleteAllCookies.class, (m) -> new DeleteAllCookies()),
//        delete_cookie_named(DeleteCookieNamed.class, (m) -> new DeleteCookieNamed(m.get("name"))),
//        get_cookie_named(GetCookieNamed.class, (m) -> new GetCookieNamed(m.get("name"))),
//        get_cookies(GetCookies.class, (m) -> new GetCookies()),
//
//        // TargetLocator (switchTo)
//        switchto_active_element(SwitchToActiveElement.class, (m) -> new SwitchToActiveElement()),
//        switchto_alert(SwitchToAlert.class, (m) -> new SwitchToAlert()),
//        switchto_default_content(SwitchToDefaultContent.class, (m) -> new SwitchToDefaultContent()),
//        switchto_frame_idx(SwitchToFrameIdx.class, (m) -> new SwitchToFrameIdx(Integer.parseInt(m.get("index")))),
//        switchto_frame_elem_by(SwitchToFrameElementBy.class, (m) -> new SwitchToFrameElementBy(m.get("by"))),
//        switchto_frame_elem(SwitchToFrameElement.class, (m) -> new SwitchToFrameElement()),
//        switchto_frame_name(SwitchToFrameByName.class, (m) -> new SwitchToFrameByName(m.get("name"))),
//        switchto_parent_frame(SwitchToParentFrame.class, (m) -> new SwitchToParentFrame()),
//        switchto_window(SwitchToWindow.class, (m) -> new SwitchToWindow(m.get("name"))),
//
//        // Alert
//        dismiss_alert(DismissAlert.class, (m) -> new DismissAlert()),
//        accept_alert(AcceptAlert.class, (m) -> new AcceptAlert()),
//        send_keys_to_alert(SendKeysToAlert.class, (m) -> new SendKeysToAlert(m.get("keys"))),
//
//        // Window
//        window_full_screen(FullScreen.class, (m) -> new FullScreen()),
//        window_get_size(WindowGetSize.class, (m) -> new WindowGetSize()),
//        setWindowSize(WindowSetSize.class, (m) -> new WindowSetSize(m.get("target"))),
//        window_maximize(WindowMaximize.class, (m) -> new WindowMaximize()),
//        window_set_position(WindowSetPosition.class, (m) -> new WindowSetPosition(m.get("x"),m.get("y"))),
//        window_get_position(WindowGetPosition.class, (m) -> new WindowGetPosition()),
//
//        // Elements
//        click(ClickElementBy.class, (m) -> new ClickElementBy(Bys.get(m.get("target")))),
//        clearElement(ClearElement.class, (m) -> new ClearElement()),
//        clearElement_by(ClearElementBy.class, (m) -> new ClearElementBy(Bys.get(m.get("by")))),
//        element_find_element_by(FindElementInElementBy.class, (m) -> new FindElementInElementBy(Bys.get(m.get("by")))),
//        element_find_elements_by(FindElementsInElementBy.class, (m)->new FindElementsInElementBy(Bys.get(m.get("by")))),
//        element_find_elements(FindElementsInElementBy.class, (m)->new FindElementsInElementBy(Bys.get(m.get("by")))),
//        element_get_attribute(GetElementAttribute.class, (m) -> new GetElementAttribute(m.get("name"))),
//        element_get_css_value(GetElementCssValue.class, (m) -> new GetElementCssValue(m.get("name"))),
//        element_get_location(GetElementLocation.class, (m) -> new GetElementLocation()),
//        element_get_rect(GetElementRect.class, (m) ->new GetElementRect()),
//        element_get_size(GetElementSize.class, (m) -> new GetElementSize()),
//        element_get_tagname(GetElementTagname.class, (m) -> new GetElementTagname()),
//        element_get_text(GetElementText.class, (m) -> new GetElementText()),
//        is_element_displayed(IsElementDisplayed.class, (m) -> new IsElementDisplayed()),
//        is_element_enabled(IsElementEnabled.class, (m) -> new IsElementEnabled()),
//        is_element_selected(IsElementSelected.class, (m) -> new IsElementSelected()),
//        element_send_keys(ElementSendKeys.class, (m) -> new ElementSendKeys(m.get("keys"))),
//        element_submit(ElementSubmit.class, (m) -> new ElementSubmit()),
//        element_get_screenshot(ElementScreenShot.class, (m) -> new ElementScreenShot()),


        ;

        private final Class<? extends WebDriverVerb> verbClass;
        private final Function<Map<String, String>, WebDriverVerb> initializer;

        Verb(Class<? extends WebDriverVerb> verbClass, Function<Map<String, String>, WebDriverVerb> initializer) {
            this.verbClass = verbClass;
            this.initializer = initializer;
        }

        public static Optional<Verb> find(String command) {
            try {
                return Optional.of(valueOf(command));
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        public WebDriverVerb resolve(Map<String, String> commandProps) {
            String cmd = commandProps.get(COMMAND);
            if (cmd == null) {
                throw new InvalidParameterException("command properties must always contain a 'command' entry'");
            }
            Verb verb = valueOf(cmd);
            return verb.initializer.apply(commandProps);
        }
    }

    public static void execute(
        long cycle,
        CommandTemplate commandTemplate,
        WebContext context,
        boolean dryrun
    ) {
        Map<String, String> cmdprops = commandTemplate.getCommand(cycle);
        logger.debug("cycle:" + cmdprops + " command:" + cmdprops);
        String command = cmdprops.get("command");
        Optional<Verb> oVerb = Verb.find(command);
        Verb verb = oVerb.orElseThrow(
            () -> new  BasicError("I don't know how to '" + command + "'")
        );
        WebDriverVerb resolve = verb.resolve(cmdprops);
        if (dryrun) {
            logger.info("skipping cycle " + cycle + " because dryrun is set to true");
            return;
        } else {
            logger.info("running cycle " + cycle + " because dryrun is set to false");
        }
        resolve.execute(context);
    }
}

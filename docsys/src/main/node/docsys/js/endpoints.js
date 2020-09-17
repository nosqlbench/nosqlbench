/**
 * Path scheme:
 * /docs/cat1/t1/t2/t3.md
 * _____                        docs prefix
 *      /                       delimiter
 *       ____                   category name
 *           /                  delimiter
 *            ___________       topic path, including separators
 *            ________          topic name, without extension
 *
 * /docs/cat1/index.md          summary doc for cat1
 * ______________________       topic path
 * __________                   category path
 */


export default {
    /**
     * construct a url for the provided logical target path, taking into account
     * the current document location, port, and logical element path.
     * When in dev mode as indicated by port 3003,
     * this auto converts service urls to point at a backend running on the same
     * host, such that the statically served content comes from the app dev
     * server, and service endpoints come from the service layer on a different port.
     */
    url(document, context, path) {
        // console.log("isDev=" + context.isDev)
        // console.log("document.location=" + JSON.stringify(document.location, null, 2))

        let origin = document.location.origin;
        let base = origin;
        base = origin.replace(":3003", ":12345")

        // console.log("base=" + base + ", path=" + path);
        let url = base + path;
        // console.log("url=" + url);
        return url;
    },
    localize(body, docurl) {
        let localized = body;
        // console.log("localizing " + details.path)
        let offset = docurl.lastIndexOf("/")
        let baseurl = docurl.slice(0, offset + 1)

        let replacer = function (match, p1, p2, p3, groups) {
            let replacement = "[" + p1 + "](" + baseurl + p2 + p3 + ")"

            // console.log("docurl:" + docurl)
            // console.log("matched:" + match)
            // console.log("baseurl:'" + baseurl + "'")
            // console.log("endpoints.baseUrlFor(document.location,isDev)'" + p1 + "'")
            // console.log("p2:'" + p2 + "'")
            // console.log("replacement:'"+replacement+"'")

            return replacement;
        }
        localized = localized.replace(/\[([a-zA-Z0-9 ]+)]\(([-._a-zA-Z0-9]+)( [=x0-9]+)?\)/g, replacer)
        if (body !== localized) {
            // console.log("rewrote:\n" + localized)
        }
        return localized;
    }
}

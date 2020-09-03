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
    url(document, context, path) {
        console.log("isDev=" + context.isDev)
        console.log("document.location=" + JSON.stringify(document.location, null, 2))

        let origin = document.location.origin;
        let base = origin;

        if (origin.includes(":3003")) {
            base = origin.replace(":3003", ":12345")
        }
        console.log("base=" + base);
        let url = base + path;
        console.log("url=" + url);
        return url;
    },
    localize(body, baseurl) {
        // console.log("localize([body],baseurl='" + baseurl + "'")
        // const regex = /\[([^/][^]]+)]/;
        // let remaining = body;
        //
        // while (remaining.)
        //
        return body;
    }
}

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
    getCategory(route, categories) {
        let active_category = categories[0];
        if (!route.path) {
            active_category = categories[0];
        } else {
            let parts = route.path.split(/\/|%2F/)
            if (parts[1]!=="docs" || parts.length===1) {
                throw "invalid path for docs: '" + route.path + "' parts[0]=" + parts[0] + " parts=" + JSON.stringify(parts,null,2)
            }
            if (parts.length>2) {
                let found = categories.find(x => x.name === parts[2]);
                if (found) {
                    active_category = found;
                }
            }
        }
        return active_category;
    },

    getTopic(route, categories, active_category) {
        let active_topic = active_category.summary_topic;

        if (!route.path) {
            active_topic = active_category.topics[0]
        } else {
            let found_topic = active_category.topics.find(x => {return x.path === route.path})
            if (found_topic) {
                active_topic = found_topic;
            }
        }
        return active_topic;

    }
}

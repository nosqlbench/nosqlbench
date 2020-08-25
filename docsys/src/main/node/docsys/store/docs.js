// https://www.mikestreety.co.uk/blog/vue-js-using-localstorage-with-the-vuex-store


/**
 categories: [
   {
     name,
     title,
     topics,
     weight,
     path
     summary_topic: {
         name,
         path,
         basename,
         categories,
         weight,
         title,
         content: mdMeta.body
     },
     topics: [
       {
         name,
         path,
         basename,
         categories,
         weight,
         title,
         content: mdMeta.body
       }
     ]
   }
 ]
 * @returns {{isDrawerOpen: boolean, isMenuLocked: boolean, active_category: null, active_topic: null, categories: []}}
 */
export const state = () => ({
    categories: [],
    active_category: null,
    active_topic: null,
    isDrawerOpen: true,
    isMenuLocked: false
});

export const getters = {
    getCategories: (state, getters) => {
        return state.categories;
    },
    getActiveCategory: (state, getters) => {
        return state.active_category;
    },
    getActiveTopic: (state, getters) => {
        return state.active_topic;
    },
    getIsMenuLocked: (state, getters) => {
        return state.isMenuLocked;
    },
    getIsDrawerOpen: (state, getters) => {
        return state.isDrawerOpen;
    },
    getActiveMarkdownContent: (state, getters) => {
        if (state.active_category===null) {
            throw "unable to load active markdown for undefined category";
        }
        if (state.active_topic===null) {
            throw "uanble to load active markdown for undefined topic";
        }
        return state.active_topic.content;
    }
}

export const mutations = {
    setActiveCategory(state, active_category) {
        state.active_category = active_category;
    },
    setActiveTopic(state, active_topic) {
        state.active_topic = active_topic;
    },
    setCategories(state, categories) {
        state.categories = categories;
    },
    // initializeStore(state) {
    //     if(localStorage.getItem('store')) {
    //         this.replaceState(
    //             Object.assign(state,JSON.parse(localStorage.getItem('store')))
    //         );
    //     }
    // },
    toggleDrawerState(state, newDrawerState) {
        if (state.isMenuLocked) {
            return;
        }
        state.isDrawerOpen = !state.isDrawerOpen;
    },
    setIsDrawerOpen(state, newDrawerState) {
        if (state.isMenuLocked) {
            return;
        }
        state.isDrawerOpen = newDrawerState;
    },
    setIsMenuLocked(state, newLockState) {
        state.isMenuLocked = newLockState;
    }
};

export const actions = {
    async setActiveCategory({commit, state, dispatch}, active_category) {
        await commit("setActiveCategory", active_category)
    },
    async setActiveTopic({commit, state, dispatch}, active_topic) {
        await commit("setActiveTopic", active_topic);
    },
    async setIsMenuLocked({commit, state, dispatch}, isMenuLocked) {
        await commit("setIsMenuLocked", isMenuLocked);
    },
    async setIsDrawerOpen({commit, state, dispatch}, isDrawerOpen) {
        await commit("setIsDrawerOpen", isDrawerOpen)
    },
    async setCategories({commit, state, dispatch}, categories) {
        await commit("setCategories", categories)
    },
    async loadCategories({commit, state, dispatch}) {
        if (state.categories === null || state.categories.length === 0) {

            let fm = require('front-matter');

            const category_data = await this.$axios.get("/docs/markdown.csv")
                .then(manifest => {
                    // console.log("typeof(manifest):" + typeof (manifest))
                    // console.log("manifest:" + JSON.stringify(manifest, null, 2))
                    return manifest.data.split("\n").filter(x => {
                        return x!==null && x.length>0
                    })
                })
                .then(async lines => {
                    let val = await Promise.all(lines.map(line => {
                        let url = "/docs" + "/" + line;
                        // console.log("url:"+url)
                        return this.$axios.get("/docs/" + line)
                            .then(res => {
                                // console.log("typeof(res):" + typeof(res))
                                return {
                                    path: line,
                                    content: res.data
                                };
                            })
                    })).then(r => {
                        // console.log("r:" + JSON.stringify(r,null,2))
                        return r
                    });
                    return val;
                    // let mapof =Object.fromEntries(val)
                    //  console.log("mapof:" + JSON.stringify(mapof, null, 2))
                    //  return mapof;
                })
                .then(fetched => {
                        return fetched.map(entry => {
                            let [, name] = entry.path.match(/(.+)\.md$/);
                            let basename = entry.path.split("/").find(x => x.includes(".md"))
                            let categories = entry.path.split("/").filter(x => !x.includes("."))

                            let mdMeta = fm(entry.content);

                            let weight = ((mdMeta.attributes.weight) ? mdMeta.attributes.weight : 0)
                            let title = ((mdMeta.attributes.title) ? mdMeta.attributes.title : basename)
                            let path = "/docs/" + entry.path

                            // console.log("path:" + entry.path)
                            return {
                                name,
                                path,
                                basename,
                                categories,
                                weight,
                                title,
                                content: mdMeta.body
                            }
                        })
                    }
                )
                .then(alltopics => {
                    // console.log("input:" + JSON.stringify(input, null, 2))
                    let categorySet = new Set();
                    alltopics.forEach(x => {
                        x.categories.forEach(y => {
                            categorySet.add(y);
                        })
                    })
                    return Array.from(categorySet).map(name => {
                        // console.log("category:" + JSON.stringify(category, null, 2))

                        let topics = alltopics.filter(x => x.categories.toString() === name);
                        // console.log("docs_in_category = " + JSON.stringify(docs_in_category, null, 2))

                        let summary_topic = topics.find(x => x.path.endsWith('index.md'));
                        let weight = summary_topic ? summary_topic.weight : 0;
                        let title = summary_topic ? (summary_topic.title ? summary_topic.title : name) : name;
                        let content = summary_topic ? (summary_topic.content ? summary_topic.content: "") : "";

                        topics = topics.filter(x => !x.path.endsWith('index.md'));
                        topics.sort((a, b) => a.weight - b.weight);

                        let path = "/docs/" + name;
                        let entry = {
                            path,
                            name,
                            title,
                            weight,
                            content,
                            topics,
                            summary_topic
                        }
                        // console.log("entry=> " + entry);
                        return entry;
                    }).sort((c1, c2) => c1.weight - c2.weight);
                })

                .catch((e) => {
                    console.error("error in loadCategories:" + e);
                })

            await dispatch("setCategories", category_data)
        }

        if (state.active_category===null) {
            commit("setActiveCategory", state.categories[0]);
        }

        if (state.active_topic===null) {
            commit("setActiveTopic", state.active_category.topics[0]);
        }

        // console.log("typeof(result):" + typeof (docinfo))
        // console.log("result:" + JSON.stringify(docinfo, null, 2))



    }
}

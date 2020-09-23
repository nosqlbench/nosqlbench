// https://www.mikestreety.co.uk/blog/vue-js-using-localstorage-with-the-vuex-store
import {mapGetters} from "vuex";
import endpoints from "@/js/endpoints";

export const state = () => ({
    workspace: 'default',
    workspaces: [],
    all_ws_files: [],
    matching_ws_files: []
});

export const getters = {
    getWorkspace: (state, getters) => {
        return state.workspace;
    },
    getWorkspaces: (state, getters) => {
        return state.workspaces;
    },
    getAllFiles: (state, getters) => {
        return state.all_ws_files;
    },
    getMatchingFiles: (state, getters) => {
        return state.matching_ws_files;
    }

    // ...mapGetters(['workspace','workspaces'])
}

export const mutations = {
    setWorkspace(state, workspace) {
        state.workspace = workspace;
    },
    setWorkspaces(state, workspaces) {
        state.workspaces = workspaces;
    },
    setAllFiles(state, files) {
        state.all_ws_files = files;
    },
    setMatchingFiles(state, files) {
        state.matching_ws_files = files;
    }
};

export const actions = {
    async importUrlToWorkspace(context, params) {
        console.log("importUrlToWorkspace(ctx," + JSON.stringify(params, null, 2));
        let workspace = params.workspace;
        let import_url = params.import_url;
        let import_as = params.import_as;
        if (!workspace || !import_url || !import_as) {
            throw("Unable to save file to workspace without params workspace, import_url, import_as");
        }
        this.$axios.$get(import_url)
            .then(res => {
                console.log('save url data:' + JSON.stringify(res, null, 2))
                return res
            }).then(data => {
            context.dispatch("putFile", {
                workspace: workspace,
                filename: import_as,
                content: data
            }).catch((e) => {
                throw "error while saving data:" + e
            })
        })
            .catch((e) => {
                throw "axios/nuxt workspaces async error:" + e
            })
    },
    async setWorkspace(context, val) {
        // console.log("committing setWorkspace:" + JSON.stringify(val));
        context.commit('setWorkspace', val);
        await context.dispatch("listWorkspaceFiles", {wsname: val})
    },
    async setWorkspaces(context, val) {
        // console.log("committing setWorkspaces:" + JSON.stringify(val));
        context.commit('setWorkspaces', val);
    },
    async listWorkspaceFiles(context, params) {
        let wsname = params.wsname;
        let contains = params.contains;
        console.log("list params:" + JSON.stringify(params, null, 2))

        let query = "?ls";
        if (contains) {
            query = query + "&contains=" + contains
        }

        await this.$axios.$get(endpoints.url(document, context, "/services/workspaces/" + wsname) + query)
            .then(res => {
                console.log("ls ws:" + JSON.stringify(res, null, 2))
                if (contains) {
                    context.commit("setContains", res["ls"]);
                } else {
                    context.commit("setFileview", res["ls"])
                }
            })
            .catch((e) => {
                throw "axios/nuxt workspaces async error:" + e
            })
    },
    async loadWorkspaces(context, reason) {
        // console.log("initializing workspaces because '" + reason + "'")
        this.$axios.$get(endpoints.url(document, context, "/services/workspaces/"))
            .then(res => {
                // console.log("axios/vuex workspaces async get:" + JSON.stringify(res));
                // console.log("committing setWorkspaces:" + JSON.stringify(res));
                context.commit('setWorkspaces', res)
            })
            .catch((e) => {
                throw "axios/nuxt workspaces async error:" + e
            })
    },
    async putFile(context, params) {
        let to_workspace = params.workspace;
        let to_filename = params.filename;
        let to_content = params.content;
        if (!to_workspace || !to_filename || !to_content) {
            console.log("params:" + JSON.stringify(params, null, 2))
            throw("Unable to save file to workspace without params having workspace, filename, content");
        }
        console.log("to_content:" + JSON.stringify(to_content, null, 2))
        const result = await this.$axios.put(endpoints.url(document, context, "/services/workspaces/" + to_workspace + "/" + to_filename), to_content)
            .then(res => {
                console.log("axios/vuex workspace put:" + JSON.stringify(res));
                return res;
            })
            .catch((e) => {
                throw "axios/vuex workspace put:" + e
            });
    },
    async initializeWorkspace(context, workspace) {
        const fresh_workspace = await this.$axios.$get(endpoints.url(document, context, "/services/workspaces/" + workspace) + "?ls")
            .then(res => {
                // console.log("axios/vuex workspace async get:" + JSON.stringify(res))
                return res;
            })
            .catch((e) => {
                throw "axios/nuxt getWorkspace async error:" + e
            })
        await context.dispatch('initWorkspaces', "workspace '" + workspace + "' added");
        // await dispatch.initWorkspaces({commit, state, dispatch}, "workspace '" + workspace + "' added")
        // await this.$store.dispatch("workspaces/initWorkspaces", "workspace '" + workspace + "' added")
        // await this.initWorkspaces({commit}, "workspace added");
        context.commit('setWorkspace', fresh_workspace.name)
        return workspace;
    },
    async purgeWorkspace(context, workspace) {
        await this.$axios.$delete(endpoints.url(document, context, "/services/workspaces/" + workspace))
            .then(res => {
                console.log("purged workspace:" + res)
                return res;
            })
            .catch((e) => {
                throw "axios/nuxt purgeWorkspace error:" + e
            })
        const found = this.state.workspaces.workspaces.find(w => w.name === workspace);
        if (!found) {
            console.log("setting active workspace to 'default' since the previous workspace '" + workspace + "' is not found")
            await context.dispatch('activateWorkspace', "default");
        }
        await context.dispatch('initWorkspaces', "workspace '" + workspace + "' purged");
    }
};

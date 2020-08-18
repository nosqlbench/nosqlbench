// https://www.mikestreety.co.uk/blog/vue-js-using-localstorage-with-the-vuex-store
import {mapGetters} from "vuex";

export const state = () => ({
    workspace: 'default',
    workspaces: []
});

export const getters = {
    getWorkspace: (state, getters) => {
        return state.workspace;
    },
    getWorkspaces: (state, getters) => {
        return state.workspaces;
    }

    // ...mapGetters(['workspace','workspaces'])
}

export const mutations = {
    setWorkspace(state, workspace) {
        state.workspace = workspace;
    },
    setWorkspaces(state, workspaces) {
        state.workspaces = workspaces;
    }
    // initializeStore(state) {
    //     if(localStorage.getItem('store')) {
    //         this.replaceState(
    //             Object.assign(state,JSON.parse(localStorage.getItem('store')))
    //         );
    //     }
    // },

};

/**
 * The base url is already set for the API by this point
 * @type {{getWorkspaces({commit: *}): void, return: ([]|[{name: string}]|[{name: string}]|default.computed.workspaces)}}
 */
export const actions = {
    async setWorkspace({commit, state, dispatch}, val) {
        console.log("committing setWorkspace:" + JSON.stringify(val));
        commit('setWorkspace', val);
    },
    async setWorkspaces({commit, state, dispatch}, val) {
        console.log("committing setWorkspaces:" + JSON.stringify(val));
        commit('setWorkspaces', val);
    },
    async initWorkspaces({commit, state, dispatch}, reason) {
        console.log("initializing workspaces because '" + reason + "'")
        this.$axios.$get("/workspaces/")
            .then(res => {
                console.log("axios/vuex workspaces async get:" + JSON.stringify(res));
                console.log("committing setWorkspaces:" + JSON.stringify(res));
                commit('setWorkspaces', res)
            })
            .catch((e) => {
                console.error("axios/nuxt workspaces async error:", e);
            })
    },
    async activateWorkspace({commit, state, dispatch}, workspace) {
        const fresh_workspace = await this.$axios.$get("/workspaces/" + workspace)
            .then(res => {
                console.log("axios/vuex workspace async get:" + JSON.stringify(res))
                return res;
            })
            .catch((e) => {
                console.error("axios/nuxt getWorkspace async error:", e)
            })
        await dispatch('initWorkspaces',"workspace '" + workspace + "' added");
        // await dispatch.initWorkspaces({commit, state, dispatch}, "workspace '" + workspace + "' added")
        // await this.$store.dispatch("workspaces/initWorkspaces", "workspace '" + workspace + "' added")
        // await this.initWorkspaces({commit}, "workspace added");
        commit('setWorkspace', fresh_workspace)
        return workspace;
    },
    async purgeWorkspace({commit, state, dispatch}, workspace) {
        await this.$axios.$delete("/workspaces/" + workspace)
            .then(res => {
                console.log("purged workspace:" + res)
                return res;
            })
            .catch((e) => {
                console.error("axios/nuxt purgeWorkspace error:", e)
            })
        await dispatch('initWorkspaces',"workspace '" + workspace + "' purged");
        // dispatch.dispatch('initWorkspaces',"workspace '" + workspace + "' purged");
        // dispatch.initWorkspaces({commit, state, dispatch}, "workspace '" + workspace + "' purged")
        const found = this.state.workspaces.workspaces.find(w => {w.name === workspace});
        if (!found) {
            console.log("setting active workspace to 'default' since the previous workspace '" + workspace + "' is not found")
            await dispatch.activateWorkspace({commit, state, dispatch}, "default");
        }


    }
};

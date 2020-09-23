// https://www.mikestreety.co.uk/blog/vue-js-using-localstorage-with-the-vuex-store
import {mapGetters} from "vuex";
import endpoints from "@/js/endpoints";

export const state = () => ({
    endpoints: null,
    enabled: null
});

export const getters = {
    getEndpoints: (state, getters) => {
        return state.endpoints;
    },
    getEnabled: (state, getters) => {
        return state.enabled;
    }
}

export const mutations = {
    setEndpoints(state, endpoints) {
        state.endpoints = endpoints;
    },
    setEnabled(state, enabled) {
        state.enabled = enabled;
    }
}

export const actions = {
    async loadEndpoints(context, reason) {
        let enabled = context.getters["getEnabled"]
        if (enabled === null || enabled === undefined) {
            console.log("loading endpoint status because '" + reason + "'")
            await this.$axios.get(endpoints.url(document, context, "/services/status"))
                .then(res => {
                    context.commit('setEndpoints', res.data.endpoints)
                    context.commit('setEnabled', res.data.enabled)
                })
                .catch((e) => {
                    console.error("axios/nuxt status async error:" + e);
                })
        }
        // else use cache defined status
    }
};

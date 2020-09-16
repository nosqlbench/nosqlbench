// https://www.mikestreety.co.uk/blog/vue-js-using-localstorage-with-the-vuex-store
import {mapGetters} from "vuex";
import endpoints from "@/js/endpoints";

export const state = () => ({
    endpoints: {},
    enabled: false
});

export const getters = {
    getEndpoints: (state, getters) => {
        return state.endpoints;
    }
}

export const mutations = {
    setEndpoints(state, endpoints) {
        state.endpoints = endpoints;
    }
}

export const actions = {
    async loadEndpoints(context, reason) {
        console.log("loading endpoint status because '" + reason + "'")
        await this.$axios.get(endpoints.url(document, context, "/services/status"))
            .then(res => {
                context.commit('setEndpoints', res)
            })
            .catch((e) => {
                console.error("axios/nuxt status async error:" + e);
            })
    }
};

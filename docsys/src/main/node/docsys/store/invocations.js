// https://www.mikestreety.co.uk/blog/vue-js-using-localstorage-with-the-vuex-store
import {mapGetters} from "vuex";

export const state = () => ({
    invocations: []
});

export const getters = {
    getInvocations: (state, getters) => {
        return state.invocations;
    }
}

export const mutations = {
    setInvocations(state, invocations) {
        state.invocations = invocations;
    }
};

export const actions = {
    async loadInvocations({commit, state, dispatch}, reason) {
        console.log("initializing scenarios because '" + reason + "'")
        this.$axios.$get("/executor/scenarios/")
            .then(res => {
                console.log("axios/vuex invocations async get:" + JSON.stringify(res));
                console.log("committing setInvocations:" + JSON.stringify(res));
                commit('setInvocations', res)
            })
            .catch((e) => {
                console.error("axios/nuxt invocations async error:", e);
            })
    }
};

// https://www.mikestreety.co.uk/blog/vue-js-using-localstorage-with-the-vuex-store
import {mapGetters} from "vuex";

export const state = () => ({
    scenarios: []
});

export const getters = {
    getScenarios: (state, getters) => {
        return state.scenarios;
    }
}

export const mutations = {
    setScenarios(state, scenarios) {
        state.scenarios = scenarios;
    }
}

export const actions = {
    async loadScenarios({commit, state, dispatch}, reason) {
        console.log("loading scenarios because '" + reason + "'")
        await this.$axios.$get("/executor/scenarios/")
            .then(res => {
                // console.log("axios/vuex scenarios async get:" + JSON.stringify(res));
                // console.log("committing setScenarios:" + JSON.stringify(res));
                commit('setScenarios', res)
            })
            .catch((e) => {
                console.error("axios/nuxt scenarios async error:", e);
            })
    },
    async stopScenario({commit, state, dispatch}, scenario_name) {
        await this.$axios.$post("/executor/stop/" + scenario_name)
            .then()
            .catch((e) => {
                console.error("axios/nuxt scenario stop error:", e);
            })
        await dispatch("loadScenarios")
    },
    async deleteScenario({commit, state, dispatch}, scenario_name) {
        await this.$axios.$delete("/executor/scenario/" + scenario_name)
            .then()
            .catch((e) => {
                console.error("axios/nuxt scenario stop error:", e);
            })
        await dispatch("loadScenarios")
    }
};

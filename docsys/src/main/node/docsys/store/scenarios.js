// https://www.mikestreety.co.uk/blog/vue-js-using-localstorage-with-the-vuex-store
import {mapGetters} from "vuex";
import endpoints from "@/js/endpoints";

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
    async loadScenarios(context, reason) {
        console.log("loading scenarios because '" + reason + "'")
        await this.$axios.$get(endpoints.url(document, context, "/services/executor/scenarios/"))
            .then(res => {
                // console.log("axios/vuex scenarios async get:" + JSON.stringify(res));
                // console.log("committing setScenarios:" + JSON.stringify(res));
                context.commit('setScenarios', res)
            })
            .catch((e) => {
                console.error("axios/nuxt scenarios async error:", e);
            })
    },
    async runScenario(context, scenario_config) {
        await this.$axios.post(endpoints.url(document, context, "/services/executor/cli"), scenario_config)
            .then(res => {
                    console.log("execute scenarios response:" + res)
                }
            )
            .catch((e) => {
                console.error("axios/nuxt cli error: " + e)
            })

    },
    async stopScenario(context, scenario_name) {
        await this.$axios.$post(endpoints.url(document, context, "/services/executor/stop/" + scenario_name))
            .then()
            .catch((e) => {
                console.error("axios/nuxt scenario stop error:", e);
            })
        await context.dispatch("loadScenarios")
    },
    async deleteScenario(context, scenario_name) {
        await this.$axios.$delete(endpoints.url(document, context, "/services/executor/scenario/" + scenario_name))
            .then()
            .catch((e) => {
                console.error("axios/nuxt scenario stop error:", e);
            })
        await context.dispatch("loadScenarios")
    }
};

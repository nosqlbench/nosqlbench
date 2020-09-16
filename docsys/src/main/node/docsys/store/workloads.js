// https://www.mikestreety.co.uk/blog/vue-js-using-localstorage-with-the-vuex-store
import {mapGetters} from "vuex";
import endpoints from "@/js/endpoints";

export const state = () => ({
    workloads: [],
    templates: {},
    searchin: ''
});

export const getters = {
    getWorkloads: (state, getters) => {
        return state.workloads;
    },
    getSearchin: (state, getters) => {
        return state.searchin;
    },
    getTemplates: (state, getters) => {
        return state.templates;
    }
}

export const mutations = {
    setWorkloads(state, workloads) {
        state.workloads = workloads;
    },
    setSearchin(state, searchin) {
        state.searchin = searchin;
    },
    setTemplates(state, templates) {
        state.templates = templates;
    }
};

export const actions = {
    async setWorkloads(context, val) {
        // console.log("committing setWorkloads:" + JSON.stringify(val));
        context.commit('setWorkloads', val);
    },
    async setTemplates(context, val) {
        // console.log("commiting setTemplates:" + JSON.stringify(val));
        context.commit("setTemplates", val);
    },
    async setSearchin(context, val) {
        // console.log("committing setsearchin:" + JSON.stringify(val));
        context.commit('setSearchin', val);
    },
    async fetchWorkloads(context, params) {
        let reason = params.reason;
        let searchin = params.searchin;
        if (reason === undefined || searchin === undefined) {
            throw "Unable to fetch workloads without a reason or searchin: " + JSON.stringify(params);
        }
        // console.log("fetching workloads because '" + reason + "'")

        context.commit("setTemplates", undefined);
        this.$axios.$get(endpoints.url(document, context, "/services/workloads/?searchin=" + searchin))
            .then(res => {
                // console.log("axios/vuex workloads async get:" + JSON.stringify(res));
                context.commit("setWorkloads", res);
            })
            .catch((e) => {
                console.error("axios/nuxt workloads async error:", e);
            })
    },
    async fetchTemplates(context, params) {
        let reason = params.reason;
        let workload = params.workload;
        let searchin = params.searchin;
        if (reason === undefined || workload === undefined || searchin === undefined) {
            throw "Unable to fetch templates for workload without a {reason,workload,searchin}: " + JSON.stringify(params);
        }
        console.log("fetching templates for '" + workload + "' because '" + reason + "'")

        this.$axios.$get(endpoints.url(document, context, "/services/workloads/parameters?workloadName=" + workload + "&" + "searchin=" + searchin))
            .then(res => {
                // console.log("axios/vuex templates async get:" + JSON.stringify(res));
                context.dispatch("setTemplates", res)
                    .then(r => {
                        console.log("setTemplates result:" + JSON.stringify(r, null, 2))
                    });
            })
            .catch((e) => {
                console.error("axios/nuxt templates async error:", e);
            })

    }
};

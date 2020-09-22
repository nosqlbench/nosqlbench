import endpoints from "@/js/endpoints";

export const state = () => ({
    pathops: []
});

export const getters = {
    getPathOps: (state, getters) => {
        return state.pathops;
    }
}

export const mutations = {
    setPathOps: (state,pathops) => {
        state.pathops=pathops
    }
}

export const actions = {
    async loadPaths(context, opts) {
        let reason = opts.reason;
        let workspace = opts.workspace;
        let filepath = (opts.filepath ? opts.filepath : "stargate.yaml")
        if (!reason || !workspace || !filepath) {
            throw "reason, workspace, filepath are all required in opts: " + JSON.stringify(opts,null,2);
        }

        await this.$axios.$get(endpoints.url(document, context, "/services/openapi/paths?filepath=" + filepath))
            .then(res => {
                context.commit('setPathOps', res)
            })
            .catch((e) => {
                console.error("axios/nuxt scenarios async error:", e);
            })
    },
}

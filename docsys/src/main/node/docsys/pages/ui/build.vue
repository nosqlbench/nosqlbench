<template>
  <v-app>
    <main-app-bar>NoSQLBench - Workload Builder</main-app-bar>

    <v-row>
      <v-main>
        <v-container fluid>
          <v-row>
            <v-alert
                v-if="!enabled"
            >This component is not online. This is only a preview. To use this, you must be running a local instance of NoSQLBench in appserver mode.</v-alert>
          </v-row>

          <v-row class="d-flex justify-center">
            <v-btn
                :disabled="buildmode==='cql'"
                title="Build CQL workload from schema"
                @click="buildmode=(buildmode==='cql' ? null : 'cql')"
            >CQL
            </v-btn>

            <v-btn
                :disabled="buildmode==='openapi'"
                title="Build OpenAPI workload from OpenAPI spec"
                @click="buildmode='openapi'"
            >OpenAPI
            </v-btn>
          </v-row>

          <v-row row v-if="buildmode==='openapi'">
            <OpenApiBuilder></OpenApiBuilder>
          </v-row>

          <v-row row v-if="buildmode==='cql'">
            <CqlBuilder></CqlBuilder>
          </v-row>

        </v-container>

      </v-main>
    </v-row>

    <v-footer app>
      <span>&copy; 2020</span>
    </v-footer>

  </v-app>
</template>
<script>
import WorkspaceSelector from "@/components/WorkspaceSelector";
import AppSelector from "@/components/AppSelector";
import MainAppBar from "@/components/MainAppBar";
import CqlBuilder from "~/components/builders/CqlBuilder";
import OpenApiBuilder from "~/components/builders/OpenApiBuilder";

export default {
  components: {
    MainAppBar,
    AppSelector,
    WorkspaceSelector,
    CqlBuilder,
    OpenApiBuilder
  },
  data(context) {
    let data = {
      buildmode: 'cql',
    };
    return data;
  },
  async asyncData({store}) {
    await store.dispatch("service_status/loadEndpoints")
    return {
      enabled: store.getters["service_status/getEnabled"]
    }
  },
  computed: {
    workspace: function () {
      return this.$store.getters["workspaces/getWorkspace"]
    }
  },
  created() {
    this.$store.dispatch('service_status/loadEndpoints')
  }
}
</script>
<style>
</style>

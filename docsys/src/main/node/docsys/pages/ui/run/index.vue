<template>
  <v-app>
    <main-app-bar>NoSQLBench - Workload Executor</main-app-bar>
    <v-container class="d-flex justify-center">

      <v-main>
        <v-row fluid class="d-flex">
          <v-col cols="12" fluid class="d-flex justify-space-between justify-center">

            <v-btn-toggle max="1" v-model="toggle_workspaces" @change="validateAndSearch()">

              <v-btn :disabled="this.toggle_builtins===undefined && this.toggle_workspaces===0">
                <v-container fluid class="d-flex">
                  <v-icon title="'include ' + workspace">mdi-folder-star</v-icon>
                  <div class="ma-2">workspace '{{ workspace }}'</div>
                  <v-icon v-if="this.toggle_workspaces===0">mdi-check</v-icon>
                </v-container>
              </v-btn>

              <v-btn :disabled="this.toggle_builtins===undefined && this.toggle_workspaces===1">
                <v-container fluid class="d-flex">
                  <v-icon title="search workspaces">mdi-folder-star-multiple</v-icon>
                  <div class="ma-2">all workspaces</div>
                  <v-icon v-if="this.toggle_workspaces===1">mdi-check</v-icon>
                </v-container>
              </v-btn>
            </v-btn-toggle>

            <v-btn-toggle v-model="toggle_builtins" @change="validateAndSearch()">
              <v-btn :disabled="this.toggle_workspaces===undefined">
                <v-container fluid class="d-flex">
                  <v-icon title="include built-in workloads">mdi-folder-open</v-icon>
                  <div class="ma-2">bundled</div>
                  <v-icon v-if="this.toggle_builtins===0">mdi-check</v-icon>

                </v-container>
              </v-btn>
            </v-btn-toggle>


          </v-col>
        </v-row>


        <v-container fluid v-if="workloads!==undefined">

          <v-select
              :items="availableWorkloads"
              item-text="description"
              item-value="workloadName"
              v-model="workloadName"
              chips
              v-on:change="loadTemplates();"
              label="Workload"
          ></v-select>

        </v-container>

        <v-container fluid>
          <v-row
              v-if="templates"
          >
            <v-col
                cols="12"
                sm="6"
                md="10"
                lg="10"
            >

              <v-card>
                <v-card-title>
                  {{ this.workloadName }}
                </v-card-title>
                <v-col
                    v-for="(item, j) in Object.keys(templates)"
                    :key="item.command"
                    cols="12"
                    sm="6"
                    md="10"
                    lg="10"
                >
                  <v-text-field
                      v-model="templates[item]"
                      :label="item"
                  >{{ item.name }}
                  </v-text-field>

                </v-col>

                <v-col cols="12">
                  <v-btn :title="runtitle" v-if="this.workloadName" v-on:click="runWorkload()">{{ runin }}</v-btn>
                </v-col>
              </v-card>
            </v-col>

          </v-row>


        </v-container>

      </v-main>
    </v-container>

    <v-footer app>
      <span>&copy; 2020</span>
    </v-footer>

  </v-app>
</template>
<script>
import WorkspaceSelector from "@/components/WorkspaceSelector";
import AppSelector from "@/components/AppSelector";
import MainAppBar from "@/components/MainAppBar";

export default {
  name: 'app-run',
  components: {
    AppSelector,
    WorkspaceSelector,
    MainAppBar
  },
  data(context) {
    let data = {
      availableWorkloads: [],
      enabled: false,
      workloadName: null,
      toggle_builtins: 0,
      toggle_workspaces: 0,
      workspace_names: ['current', 'all'],
      sample: {
        "workspace": "default",
        "yamlPath": "test1.yaml",
        "scenarioNames": [
          "default",
          "main"
        ],
        "templates": {
          "keyspace": "a",
          "main-cycles": "10000000",
          "rampup-cycles": "10000000",
          "read_cl": "LOCAL_QUORUM",
          "read_partition_ratio": "1",
          "read_row_ratio": "1",
          "write_cl": "LOCAL_QUORUM",
          "write_ratio": "1"
        },
        "description": "test1",
        "workloadName": "test1"
      },
    };
    return data;
  },
  computed: {
    searchin: function () {
      let searchin = Array();
      if (this.toggle_workspaces === 0) {
        searchin.push(this.workspace);
      } else if (this.toggle_workspaces === 1) {
        console.log("workspaces typeof: '" + typeof (this.workspaces) + "'");
        this.workspaces.forEach(w => {
          searchin.push(w.name);
        })
      }
      if (this.toggle_builtins === 0) {
        searchin.push("builtins");
      }
      let joined = searchin.join(",");
      console.log("joined:'" + joined + "'")
      return joined;
    },
    runin: function () {
      return "Run Workload in " + this.$store.getters["workspaces/getWorkspace"];
    },
    runtitle: function () {
      return "Click to run this workload in the '" + this.workspace + "' workspace, or change the workspace in the app bar.\n"
    },
    workspace: function () {
      return this.$store.getters["workspaces/getWorkspace"]
    },
    workspaces: function () {
      return this.$store.getters["workspaces/getWorkspaces"]
    },
    workloads: function () {
      return this.$store.getters["workloads/getWorkloads"];
    },
    templates: function () {
      return this.$store.getters["workloads/getTemplates"];
    },
    workloadNames: function () {
      for (const [key, value] of Object.entries(this.workloads)) {
        console.log("key=[" + key + "] value=[" + value + "]");
      }
    }
  },
  watch: {
    toggle_builtins: function (val) {
      this.validateAndSearch();
    },
    toggle_workspaces: function (val) {
      this.validateAndSearch();
    }
  },
  created() {
    this.validateAndSearch();
    this.$store.subscribe((mutation, state) => {
      console.log("mutation type " + mutation.type);
      if (mutation.type === 'workloads/setWorkloads') {
        console.log("detected update to workloads:" + JSON.stringify(this.workloads));
        this.availableWorkloads = state.workloads.workloads;
      }
    });
  },
  methods: {
    validateAndSearch() {

      let params = {
        searchin: this.searchin,
        reason: "search params changed"
      }
      this.$store.dispatch("workloads/fetchWorkloads", params);
    },
    loadTemplates() {
      let params = {
        workload: this.workloadName,
        searchin: this.searchin,
        reason: "workload selected"
      }
      this.$store.dispatch("workloads/fetchTemplates", params);
    },
    runWorkload() {
      console.log("running workload...")
      let workload = this.availableWorkloads.filter(w => w.workloadName===this.workloadName);
      let erq = {
        name: "run_"+workload.workloadName,
        workspace: this.workspace,
        commands: [this.workloadName]
      }
      console.log("submitting:" + JSON.stringify(erq));
      this.$axios.$post("services/executor/cli",erq);
    }
  }
}
</script>
<style>
/*.container {*/
/*  margin: 0 auto;*/
/*  display: flex;*/
/*  justify-content: center;*/
/*  align-items: center;*/
/*  text-align: center;*/
/*}*/

/*.title {*/
/*  font-family: 'Quicksand', 'Source Sans Pro', -apple-system, BlinkMacSystemFont,*/
/*  'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;*/
/*  display: block;*/
/*  font-weight: 300;*/
/*  font-size: 100px;*/
/*  color: #35495e;*/
/*  letter-spacing: 1px;*/
/*}*/

/*.subtitle {*/
/*  font-weight: 300;*/
/*  font-size: 42px;*/
/*  color: #526488;*/
/*  word-spacing: 5px;*/
/*  padding-bottom: 15px;*/
/*}*/

/*.links {*/
/*  padding-top: 15px;*/
/*}*/
</style>

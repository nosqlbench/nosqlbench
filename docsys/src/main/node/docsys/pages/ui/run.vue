<template>
  <v-app>
    <main-app-bar>NoSQLBench - Workload Executor</main-app-bar>
    <v-container class="d-flex justify-center">

      <v-main>
        <v-row fluid class="d-flex">
          <v-col cols="12" fluid class="d-flex justify-space-between justify-center">

            <!--            <v-btn-toggle max="1" v-model="toggle_workspaces" @change="validateAndSearch()">-->

            <!--              <v-btn :disabled="this.toggle_builtins===undefined && this.toggle_workspaces===0">-->
            <!--                <v-container fluid class="d-flex">-->
            <!--                  <v-icon title="'include ' + workspace">mdi-folder-star</v-icon>-->
            <!--                  <div class="ma-2">workspace '{{ workspace }}'</div>-->
            <!--                  <v-icon v-if="this.toggle_workspaces===0">mdi-check</v-icon>-->
            <!--                </v-container>-->
            <!--              </v-btn>-->

            <!--              <v-btn :disabled="this.toggle_builtins===undefined && this.toggle_workspaces===1">-->
            <!--                <v-container fluid class="d-flex">-->
            <!--                  <v-icon title="search workspaces">mdi-folder-star-multiple</v-icon>-->
            <!--                  <div class="ma-2">all workspaces</div>-->
            <!--                  <v-icon v-if="this.toggle_workspaces===1">mdi-check</v-icon>-->
            <!--                </v-container>-->
            <!--              </v-btn>-->
            <!--            </v-btn-toggle>-->

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

        <v-row fluid v-if="workloads!==undefined">

          <!--              :item-text="workspace"-->
          <!--              :item-value="workloadName"-->
          <!--              v-model="workloadName"-->
          <v-select
              :items="availableWorkloads"
              item-text="workloadName"
              item-value="workloadName"
              v-model="selected"
              v-on:change="loadTemplates()"
              label="Workload"
          ></v-select>

        </v-row>

        <!-- TEMPLATES -->

        <v-main justify-start align-start class="d-inline-block pa-4 ma-10">
          <v-row no-gutters v-if="templates">
            <v-card v-for="(item, j) in Object.keys(templateparams)"
                    :key="item"
                    class="ma-4 pa-4"
            >
              <!--            <v-card-title>{{item}}</v-card-title>-->
              <v-card-title class="ma-0 pa-0">{{ item }}</v-card-title>
              <v-text-field hide-details v-model="templateparams[item]" align="center"></v-text-field>
              <!--                <v-card-title>{{ this.workloadName }}</v-card-title>-->
              <!--                <v-row v-for="(item, j) in Object.keys(templates)" :key="item.command">-->
              <!--                  <v-text-field v-model="templates[item]" :label="item">{{ item.name }}></v-text-field>-->
              <!--                </v-row>-->
            </v-card>
          </v-row>

          <!--          <v-row v-if="templates">-->
          <!--            <v-col>-->
          <!--              <v-card>-->
          <!--                <v-card-title>{{ this.workloadName }}</v-card-title>-->
          <!--                <v-row v-for="(item, j) in Object.keys(templates)" :key="item.command">-->
          <!--                  <v-text-field v-model="templates[item]" :label="item">{{ item.name }}></v-text-field>-->
          <!--                </v-row>-->
          <!--              </v-card>-->
          <!--            </v-col>-->
          <!--          </v-row>-->

          <v-row>
            <v-col></v-col>
          </v-row>

          <v-row>
            <v-col cols="12">
              <v-btn :title="runtitle" v-if="this.selected" v-on:click="runWorkload()">{{ runin }}</v-btn>
            </v-col>
          </v-row>

        </v-main>

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
      extraparams: {},
      templateparams: {},
      availableWorkloads: [],
      enabled: false,
      selected: null,
      toggle_builtins: false,
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
      return "Run Workload";
    },
    runtitle: function () {
      return "Run this workload in workspace [" + this.workspace + "].\n"
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
    }
    // ,
    // workloadNames: function () {
    //   for (const [key, value] of Object.entries(this.workloads)) {
    //     console.log("key=[" + key + "] value=[" + value + "]");
    //   }
    // }
  },
  watch: {
    toggle_builtins: function (val) {
      this.validateAndSearch();
    },
    toggle_workspaces: function (val) {
      this.validateAndSearch();
    },
    templates: function (val) {
      console.log("templates property changed");
      if (val === undefined) {
        this.templateparams = undefined;
      } else {
        this.templateparams = {};
        Object.keys(val).forEach(k => {
          console.log("k:" + k + " = " + val[k])
          if (!this.templateparams[k]) {
            this.templateparams[k] = val[k];
          }
        })
      }
    }
  },
  created() {
    this.validateAndSearch();
    this.$store.subscribe((mutation, state) => {
      if (mutation.type === 'workloads/setWorkloads') {
        // console.log("detected update to workloads:" + JSON.stringify(this.workloads));
        this.availableWorkloads = state.workloads.workloads;
      } else if (mutation.type === 'workspaces/setWorkspace') {
        // console.log("detected update to workspace:" + JSON.stringify(this.workspace));
        this.validateAndSearch();
      }
      // else if (mutation.type === 'workloads/setTemplates') {
      //   console.log("detected update to templates:" + JSON.stringify(this.workspace));
      //   Object.keys(this.templates).forEach(t => {
      //     console.log("template:" + t)
      //     if (this.templateparams[t]) {
      //       this.templateparams[t] = this.templates[t];
      //       console.log("added '" + this.templates[t])
      //     }
      //   })
      // }
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
        workload: this.selected,
        searchin: this.searchin,
        reason: "workload selected"
      }
      this.$store.dispatch("workloads/fetchTemplates", params);
      let templates = this.$store.getters["workloads/getTemplates"]

      console.log("templates?" + JSON.stringify(templates, null, 2))
      // Object.keys(this.templates).forEach(t => {
      //   console.log("t:" + t)
      // //   if (!this.templateparams[t]) {
      // //     this.templateparams[t]=this.templates[t];
      // //   }
      // })
    },
    runWorkload() {
      console.log("running workload '" + this.selected + "'")
      // let workload = this.availableWorkloads.find(w => w.workloadName === this.selected);
      let commands=[];

      commands.push(this.selected);
      Object.keys(this.templates).forEach(k => {
        if (this.templateparams && this.templateparams[k]!==this.templates[k]) {
          commands.push(k+"="+this.templateparams[k])
        }
      })
      Object.keys(this.extraparams).forEach(k => {
        commands.push(k+"="+this.extraparams[k])
      })
      let erq = {
        scenario_name: this.selected + "_DATESTAMP",
        workspace: this.workspace,
        commands
      }
      console.log("submitting:" + JSON.stringify(erq));
      this.$store.dispatch("scenarios/runScenario", erq);
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

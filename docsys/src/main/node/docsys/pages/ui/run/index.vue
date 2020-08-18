<template>
  <v-app>

    <v-app-bar app dark color="secondary">
      <v-toolbar-title>NoSQLBench</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items>
        <app-selector></app-selector>
        <workspace-selector></workspace-selector>
        <v-btn text href="https://github.com/nosqlbench/nosqlbench/wiki/Submitting-Feedback">SUBMIT FEEDBACK</v-btn>
      </v-toolbar-items>
    </v-app-bar>

    <v-layout
        justify-center
        align-center>

      <v-main>
        <v-container fluid v-if="enabled">

          <v-select
              :items="workloadNames"
              v-model="workloadName"
              chips
              v-on:change="getTemplates();"
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
                  {{ workloadName }}
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
                  <v-btn v-if="workloadName" v-on:click="runWorkload()">Run Workload</v-btn>
                </v-col>
              </v-card>
            </v-col>

          </v-row>


        </v-container>

      </v-main>
    </v-layout>

    <v-footer app dark color="secondary">
      <span>&copy; 2020</span>
    </v-footer>

  </v-app>
</template>
<script>
import get_data from '~/mixins/get_data.js';
import WorkspaceSelector from "~/components/WorkspaceSelector";
import AppSelector from "@/components/AppSelector";

export default {
  name: 'app-run',
  mixins: [get_data],
  components: {
    AppSelector,
    WorkspaceSelector
  },
  computed: {},
  methods: {
    async getTemplates() {
      const data = await this.$axios.$get('/workloads/parameters?workloadName=' + this.workloadName)
      if (!data.err) {
        this.$data.templates = data;
      }
    },
  },
  data(context) {
    let data = {
      workloadNames: [],
      enabled: false,
      workloadName: null,
      templates: null,
    };
    return data;
  },
  async asyncData({$axios, store}) {
    let enabled = await $axios.$get("/status")
        .then(res => {
          return res
        })
        .catch((e) => {
          console.log("back-end not found");
        })
    let workloadNames = await $axios.$get("/workloads")
        .then(res => {
          return res
        })
        .catch((e) => {
          console.log("back-end not found");
        })
    let workspaces = await $axios.$get("/workspaces")
        .then(res => {
          return res
        }).catch((e) => {
          console.log("back-end not found")
        });


    return {
      enabled: enabled,
      workloadNames: workloadNames,
      workspaces: workspaces
    }
  },
}
</script>
<style>
.container {
  margin: 0 auto;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.title {
  font-family: 'Quicksand', 'Source Sans Pro', -apple-system, BlinkMacSystemFont,
  'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  display: block;
  font-weight: 300;
  font-size: 100px;
  color: #35495e;
  letter-spacing: 1px;
}

.subtitle {
  font-weight: 300;
  font-size: 42px;
  color: #526488;
  word-spacing: 5px;
  padding-bottom: 15px;
}

.links {
  padding-top: 15px;
}
</style>

<template>
  <v-app>
    <v-app-bar app dark color="secondary">
      <v-toolbar-title>NoSQLBench - Workspaces</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items>
        <workspace-selector></workspace-selector>

        <v-btn title="start a new workspace" @click="startNewWorkspace()">
          <v-icon>mdi-folder-plus-outline</v-icon>
        </v-btn>
        <v-btn title="upload a workspace zip file">
          <v-icon>mdi-folder-upload</v-icon>
        </v-btn>
        <v-btn title="download workspaces.zip">
          <v-icon>mdi-briefcase-download</v-icon>
        </v-btn>
        <v-btn title="upload workspaces.zip">
          <v-icon>mdi-briefcase-upload</v-icon>
        </v-btn>
<!--        <v-btn text href="https://github.com/nosqlbench/nosqlbench/wiki/Submitting-Feedback">SUBMIT FEEDBACK</v-btn>-->
      </v-toolbar-items>
    </v-app-bar>

    <!--
      {
        "summary": {
          "total_bytes": 24,
          "total_files": 1,
          "last_change": 1597293397043,
          "last_file_changed": "README.md"
        },
        "name": "default",
        "modified": 1597293397043,
        "changed": "README.md (1H17M27S ago)"
      }
    ]
    -->

    <v-main justify-start align-start class="d-flex pa-4 ma-4">
      <v-main>
        <v-card max-width="344" v-for="(workspace,w) in workspaces" :key="w" class="pa-4 ma-4">
          <v-card-title title="workspace name">{{ workspace.name }}</v-card-title>
          <v-card-subtitle title="last change">{{ abbrev(workspace.summary.last_changed_filename) }}</v-card-subtitle>
          <v-divider></v-divider>
          <v-list align-start>
            <v-simple-table>
              <tbody>
              <tr><td>Bytes</td><td>{{ workspace.summary.total_bytes}}</td></tr>
              <tr><td>Files</td><td>{{ workspace.summary.total_files}}</td></tr>
              </tbody>
            </v-simple-table>
            <v-divider></v-divider>

            <v-list-item>
              <v-btn title="view details of workspace">
                <v-icon>mdi-magnify</v-icon>
              </v-btn>

              <v-btn title="use this workspace">
                <v-icon>mdi-play</v-icon>
              </v-btn>

              <v-btn title="download zipped workspace">
                <v-icon>mdi-folder-download</v-icon>
              </v-btn>

              <v-spacer></v-spacer>

              <v-btn title="purge workspace">
                <v-icon @click="purgeWorkspace(workspace.name)">mdi-trash-can</v-icon>
              </v-btn>

            </v-list-item>
          </v-list>
        </v-card>
      </v-main>
    </v-main>

  </v-app>
</template>

<script>

import WorkspaceSelector from "~/components/WorkspaceSelector";

export default {
  name: "workspaces.vue",
  components: {
    WorkspaceSelector
  },
  data(context) {
    let data = {
      workspaces: [
        {
          "name": "test"
        }
      ]
    };
    return data;
  },
  methods: {
    abbrev(name) {
      return name;
    },
    purgeWorkspace: function(ws) {
      console.log("purging " + ws);
      this.$axios.$delete("/services/workspaces/" + ws)
      .then(res => { return res })
      .catch((e) => {
        console.log("error: " + e)
      });
      this.$forceUpdate();
    },
    startNewWorkspace() {
      console.log("starting new workspace");
    }
  },
  async asyncData({$axios, store}) {
    let enabled = await $axios.$get("/services/status")
        .then(res => {
          return res
        })
        .catch((e) => {
          console.log("back-end not found");
        })
    let workspaces = await $axios.$get("/services/workspaces/")
        .then(res => {
          return res
        })
        .catch((e) => {
          console.log("back-end not found");
        })
    return {enabled, workspaces}
  }
}
</script>

<style scoped>

</style>

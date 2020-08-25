<template>
  <v-app>
    <main-app-bar></main-app-bar>

    <!--        <workspace-selector @changed="seenChange(workspace)"></workspace-selector>-->

    <!--        <v-btn title="start a new workspace" @click="startNewWorkspace()">-->
    <!--          <v-icon>mdi-folder-plus-outline</v-icon>-->
    <!--        </v-btn>-->

    <!--        <v-btn icon title="upload a workspace zip file">-->
    <!--          <v-icon>mdi-folder-upload</v-icon>-->
    <!--        </v-btn>-->
    <!--        <v-btn icon title="download workspaces.zip">-->
    <!--          <v-icon>mdi-briefcase-download</v-icon>-->
    <!--        </v-btn>-->
    <!--        <v-btn icon title="upload workspaces.zip">-->
    <!--          <v-icon>mdi-briefcase-upload</v-icon>-->
    <!--        </v-btn>-->

    <v-content justify-start align-start class="d-inline-block pa-4 ma-10">
      <div class="row no-gutters">
        <v-card min-width="300" max-width="300" max-height="400" raised elevation="5" v-for="(cardspace,w) in workspaces" :key="w"
                class="pa-4 ma-4">
          <v-row>
            <v-card-title title="workspace name">{{ cardspace.name }}</v-card-title>
            <v-icon v-if="workspace === cardspace.name">mdi-check-bold</v-icon>
          </v-row>

          <v-card-subtitle title="last change">{{ abbrev(cardspace.summary.last_changed_filename) }}</v-card-subtitle>
          <v-divider></v-divider>
          <v-list align-start>
            <v-simple-table>
              <tbody>
              <tr>
                <td>Bytes</td>
                <td>{{ cardspace.summary.total_bytes }}</td>
              </tr>
              <tr>
                <td>Files</td>
                <td>{{ cardspace.summary.total_files }}</td>
              </tr>
              </tbody>
            </v-simple-table>
            <v-divider></v-divider>

            <v-list-item>
              <v-btn title="view details of workspace">
                <v-icon>mdi-magnify</v-icon>
              </v-btn>

              <v-spacer></v-spacer>

              <v-btn title="download zipped workspace">
                <v-icon>mdi-folder-download</v-icon>
              </v-btn>

              <v-spacer></v-spacer>

              <v-btn title="purge workspace">
                <v-icon @click="purgeWorkspace(cardspace.name)">mdi-trash-can</v-icon>
              </v-btn>

            </v-list-item>
          </v-list>
        </v-card>
      </div>
    </v-content>

  </v-app>
</template>

<script>

import WorkspaceSelector from "~/components/WorkspaceSelector";
import {mapActions, mapGetters, mapMutations} from "vuex";
import AppSelector from "@/components/AppSelector";
import MainAppBar from "@/components/MainAppBar";

export default {
  name: "workspaces.vue",
  components: {
    MainAppBar,
    AppSelector,
    WorkspaceSelector
  },
  data(context) {
    let data = {};
    return data;
  },
  computed: {
    workspace: {
      get() {
        return this.$store.getters["workspaces/getWorkspace"]
      },
      set(val) {
        this.$store.dispatch("workspaces/setWorkspace", val)
      }
    },
    workspaces: {
      get() {
        return this.$store.getters["workspaces/getWorkspaces"]
      },
      set(val) {
        this.$store.dispatch("workspaces/setWorkspaces", val)
      }
    },
  },
  methods: {
    abbrev(name) {
      return name;
    },
    purgeWorkspace: function (ws) {
      console.log("purging " + ws);
      this.$store.dispatch('workspaces/purgeWorkspace', ws);
      // this.$store.dispatch("workspaces/setWorkspace")
      this.$forceUpdate();
    },
  },
  created() {
    console.log("created component...");
    this.$store.dispatch('workspaces/initWorkspaces', "workspace panel load");
  }
}

</script>

<style>
</style>

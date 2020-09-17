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
    this.$store.dispatch('workspaces/initWorkspaces', "workspace panel load");
  }
}

</script>

<style>
</style>

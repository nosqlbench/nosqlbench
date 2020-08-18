<template>
  <v-app>
    <v-app-bar app dark color="secondary">
      <v-toolbar-title>NoSQLBench - Workspaces</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items>
        <app-selector></app-selector>
        <workspace-selector></workspace-selector>
        <!--        <workspace-selector @changed="seenChange(workspace)"></workspace-selector>-->

        <!--        <v-btn title="start a new workspace" @click="startNewWorkspace()">-->
        <!--          <v-icon>mdi-folder-plus-outline</v-icon>-->
        <!--        </v-btn>-->
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

    <v-main justify-start align-start class="d-inline-block pa-4 ma-10">
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

              <!--              <v-btn title="use this workspace">-->
              <!--                <v-icon>mdi-play</v-icon>-->
              <!--              </v-btn>-->

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
    </v-main>

  </v-app>
</template>

<script>

import WorkspaceSelector from "~/components/WorkspaceSelector";
import {mapActions, mapGetters, mapMutations} from "vuex";
import AppSelector from "@/components/AppSelector";

export default {
  name: "workspaces.vue",
  components: {
    AppSelector,
    WorkspaceSelector
  },
  data(context) {
    let data = {
      // workspace: this.$store.state.workspaces.workspace,
      // workspaces: [
      //   {
      //     "name": "test"
      //   }
      // ]
    };
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
    // workspace: {
    //   get: function () {
    //     return this.$store.getters.workspaces.workspace;
    //   },
    //   set: function (val) {
    //     return this.$store.dispatch('workspaces/getWorkspace')
    //   }
    // },
    // workspaces: {
    //   get: function () {
    //     return this.$store.getters.workspaces.workspaces;
    //   },
    //   set: function (val) {
    //     return this.$store.dispatch('workspaces/getWorkspaces');
    //   }
    // }
  },
  methods: {
    // ...mapActions({
    //   workspaces: "workspaces/setWorkspaces",
    //   // workspace: "workspaces/setWorkspace"
    // }),
    abbrev(name) {
      return name;
    },
    purgeWorkspace: function (ws) {
      console.log("purging " + ws);
      this.$store.dispatch('workspaces/purgeWorkspace', ws);
    },
  },
  created() {
    console.log("created component...");
    // this.$store.subscribe((mutation, state) => {
    //   console.log("mutation type " + mutation.type);
    //   if (mutation.type === 'workspaces/setWorkspace') {
    //     this.workspace = this.$store.state.workspaces.workspace;
    //   } else if (mutation.type === 'workspaces/setWorkspaces') {
    //     this.workspacaes = this.$store.state.workspaces.workspaces;
    //   } else {
    //     console.error("Unrecognized mutation", mutation)
    //   }
    // })
    this.$store.dispatch('workspaces/initWorkspaces', "workspace panel load");
  }
}

</script>

<style scoped>

</style>

<template>
  <v-container>
    <v-text-field dense
                  full-width
                  label="Name of new workspace"
                  v-if="mode==='adding'"
                  v-model="new_workspace"
                  ref="new_workspace_input"
                  hint="workspace name"
                  @blur="commitWorkspace(new_workspace)"
                  @keydown.enter="commitWorkspace(new_workspace)"
    ></v-text-field>
    <v-select dense
              hide-details="auto"
              label="workspace"
              v-if="mode==='showing'"
              v-model="workspace"
              :items="workspaces"
              item-text="name"
              item-value="name"
    >
      <template v-slot:append-item>
        <v-list-item>
          <v-btn @click="addWorkspace()">+ Add Workspace</v-btn>
        </v-list-item>
      </template>
    </v-select>
  </v-container>
</template>
<script>

export default {
  name: 'workspace-selector',
  data() {
    let mode = "showing";
    let new_workspace = "";
    return {mode, new_workspace}
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
    }
  },
  methods: {
    addWorkspace: function () {
      this.mode = "adding";
    },
    commitWorkspace: function ({$store}) {
      console.log("commit:" + JSON.stringify(this.new_workspace));
      this.$store.dispatch("workspaces/activateWorkspace", this.new_workspace);
      this.mode = "showing";
      //
      //
      // WorkspaceService.getWorkspace({'name': this.new_workspace})
      //     .then(res => {
      //       console.log("async create workspace: " + JSON.stringify(res));
      //     })
      //     .then(res => {
      //       return WorkspaceService.getWorkspaces();
      //     })
      //     .then(res => {
      //       console.log("get workspaces: " + JSON.stringify(res));
      //       this.setWorkspaces(res)
      //       this.setWorkspace(this.new_workspace)
      //       this.new_workspace = "";
      //       this.mode = "showing"
      //     })
      //     .catch((e) => {
      //       console.log("error in commitWorkspaces: " + e)
      //     })
    }
    // async getWorkspaces() {
    //   const response = await WorkspaceService.getWorkspaces()
    //   this.setWorkspaces()
    // },
    // setWorkspaces: function (workspaces) {
    //   this.workspaces = workspaces;
    //   this.$store.commit('workspaces/setWorkspaces', this.workspaces);
    // },
    // selectWorkspace: function (selected) {
    //   this.workspace = selected;
    //   this.$store.commit('workspaces/setWorkspace', this.workspace);
    // }
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
    this.$store.dispatch('workspaces/initWorkspaces', "selector load");
  }
}
</script>
<style>
</style>

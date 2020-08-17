<template>
  <v-container>
    <v-text-field dense
                  label="Name of new workspace"
                  v-if="mode=='adding'"
                  v-model="new_workspace"
                  ref="new_workspace_input"
                  hint="workspace name"
                  @blur="commitWorkspace(new_workspace)"
                  @keydown.enter="commitWorkspace(new_workspace)"
    ></v-text-field>
    <v-select dense
              label="workspace"
              v-if="mode=='showing'"
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
  data(context) {
    let data = {
      new_workspace: "",
      mode: "showing",
      workspaces: [{name: 'default'}],
      workspace: {name: 'default'},
      enabled: false
    };
    return data;
  },
  methods: {
    addWorkspace: function (evt) {
      this.mode = "adding";
      setTimeout(() => {
        this.$refs.new_workspace_input.$el.focus()
      });
      console.log("add evt:" + JSON.stringify(evt));
    },
    commitWorkspace: function (evt) {
      console.log("commit evt:" + JSON.stringify(evt));
      let committed = this.$axios.$get("/services/workspaces/" + evt)
          .then(res => {
            return res;
          })
          .catch((e) => {
            console.log("create: error: " + e)
          });
      console.log("committed: " + JSON.stringify(committed))
      this.workspaces = this.$axios.$get("/services/workspaces/")
          .then(res => {
            console.log("workspaces async:" + JSON.stringify(res));
            return res;
          })
          .catch((e) => {
            console.log("refresh error: " + e)
          });
      this.workspace = evt;
      this.mode = "showing"
      this.$forceUpdate();
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
<style>
</style>

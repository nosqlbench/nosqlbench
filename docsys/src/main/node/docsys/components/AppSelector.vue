<template>
  <div class="d-flex align-center ma-2 pa-1 text-center">

    <v-btn to="/ui/build/" title="Build a workload from a schema">Build</v-btn>
    <v-btn to="/ui/run/" title="Run a workload">Run</v-btn>
    <v-btn to="/ui/watch/" title="Watch workload status">Watch</v-btn>
    <v-btn to="/docs/" title="Documentation">Docs</v-btn>

  </div>
</template>

<script>
export default {
  name: 'app-selector',
  data() {
    let data = {
      empty: [],
      apps: ['build', 'run', 'status', 'workspaces'],
      thisapp: 'build'
    };
    return data;
  }
}
</script>

<style>
</style>

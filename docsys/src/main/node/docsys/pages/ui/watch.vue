<template>
  <v-app>
    <main-app-bar>NoSQLBench - Execution Status</main-app-bar>

    <v-main justify-start align-start class="d-inline-block pa-4 ma-10">
      <div class="row no-gutters">

        <!-- SCENARIO CARD -->

        <v-card min-width="300" max-width="300" max-height="400" raised elevation="3"
                v-for="(scenario,w) in scenarios" :key="w"
                class="pa-4 ma-4"
                :title="JSON.stringify(scenario,null,2)"
        :color="scenario.state==='Running' ? 'accent' : 'default'">

          <v-row align-content="start" align="start">
            <v-card-title>{{ scenario.scenario_name }}</v-card-title>
            <v-card-subtitle>{{ scenario.state }}</v-card-subtitle>
            <!--            <v-icon>mdi-magnify</v-icon>-->
          </v-row>

          <!-- Scenario Controls -->

          <v-row>
            <v-btn
                v-if="scenario.state==='Running'"
                @click="stop_scenario(scenario)"
                :title="'Stop scenario ' + scenario.scenario_name"
            ><v-icon>mdi-stop</v-icon>

            </v-btn>
            <!--            <v-btn v-if="scenario.state!=='Running'">-->
            <!--              <v-icon>mdi-play</v-icon>-->
            <!--            </v-btn>-->
            <v-btn v-if="scenario.state==='Finished' || scenario.state==='Errored'"
                   :title="'Purge scenario ' + scenario.scenario_name"
                   @click="purge_scenario(scenario)"
            ><v-icon>mdi-delete</v-icon>
            </v-btn>

            <v-btn v-if="scenario.state==='Scheduled'"
            :title="'cancel scenario ' + scenario.scenario_name"
                   @click="purge_scenario(scenario)"
            ><v-icon>mdi-cancel</v-icon></v-btn>
          </v-row>

          <v-divider></v-divider>

          <!-- Activities -->
          <!--          <v-card-subtitle>activities:</v-card-subtitle>-->
          <v-row v-for="(progress,p) in scenario.progress" :key="p">
            <v-card min-width="290" :title="JSON.stringify(progress,null,2)">
              <v-card-subtitle class="pa-2 ma-2">{{ progress.name }}</v-card-subtitle>
              <v-progress-linear v-if="progress.completed"
                                 :title="(progress.completed*100.0).toFixed(4)"
                                 :value="progress_of(progress.completed)"
                                 :color="colorof(progress.state)"
                                 height="10"
              ></v-progress-linear>
              <v-card-text v-if="progress.state==='Running'">
                {{ (progress.eta_millis / 60000).toFixed(2) + " minutes remaining" }}
              </v-card-text>
              <v-card-text v-if="scenario.result && scenario.result.error">
                {{ JSON.stringify(scenario.result.error, null, 2) }}
              </v-card-text>
            </v-card>
          </v-row>
        </v-card>


      </div>
    </v-main>


    <!-- FOOTER -->

    <v-footer :color="this.paused ? 'secondary' : 'unset'" app>
      <v-row align="middle">

        <v-progress-circular color="#AAAAAA"
                             width="1"
                             :title="'refresh in '+(this.beats_per_refresh-this.beats_in_refresh) + 's (' + this.beats_per_suffix + ')'"
                             :value="(this.beats_in_refresh/this.beats_per_refresh)*100"
                             @click="do_refresh"
        >{{ beats_per_refresh - beats_in_refresh }}
        </v-progress-circular>
        <v-icon v-if="!this.paused" @click="pause()">mdi-pause</v-icon>
        <v-icon v-if="this.paused" @click="unpause()">mdi-play</v-icon>
        <v-icon @click="scale_up">mdi-plus</v-icon>
        <v-icon @click="scale_down">mdi-minus</v-icon>
      </v-row>
    </v-footer>

  </v-app>
</template>

<script>

import WorkspaceSelector from "@/components/WorkspaceSelector";
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
    let data = {
      beat: false,
      beats_per_refresh: 10,
      beats_in_refresh: 0,
      beats_per_suffix: '10s',
      scales: [
        [1, '1s'], [5, '5s'], [10, '10s'], [20, '20s'], [60, '60s'], [300, '5m'], [600, '10m'], [1800, '15m']
      ],
      beat_tick: 1000,
      heart_is_beating: false,
      paused: false,

    };
    return data;
  },
  computed: {
    scenarios: {
      get() {
        return this.$store.getters["scenarios/getScenarios"]
      }
    },
  },
  methods: {
    colorof(state) {
      if (state==='Running') {
        return "success";
      }
      if (state==='Errored') {
        return "error"
      }
      return "blue";
    },
    scale_up() {
      let idx =Array.from(Array(this.scales.length), (x,i) => i)
      .find(y => {return this.scales[y][0]===this.beats_per_refresh})
      idx = Math.min(idx+1,this.scales.length-1);
      console.log("idx:"+idx)
      this.beats_per_refresh=this.scales[idx][0];
      this.beats_per_suffix = this.scales[idx][1];
    },
    scale_down() {
      let idx =Array.from(Array(this.scales.length), (x,i) => i)
          .find(y => {return this.scales[y][0]===this.beats_per_refresh})
      idx = Math.max(idx-1,0);
      console.log("idx:"+idx)
      this.beats_per_refresh=this.scales[idx][0];
      this.beats_per_suffix = this.scales[idx][1];
    },
    stop_scenario(scenario) {
      console.log("stopping scenario: " + scenario.scenario_name);
      this.$store.dispatch("scenarios/stopScenario", scenario.scenario_name)
      this.do_refresh();
    },
    purge_scenario(scenario) {
      console.log("purging scenario: " + scenario.scenario_name);
      this.$store.dispatch("scenarios/deleteScenario", scenario.scenario_name);
      this.do_refresh();
    },
    pause() {
      this.paused = true;
    },
    unpause() {
      this.paused = false;
      this.heartbeat();
    },
    heartbeat() {
      this.heart_is_beating = false;
      if (this.paused) {
        return;
      }
      let seconds = new Date().getSeconds();
      // console.log("seconds:" + seconds);
      this.beat = (seconds % 2) === 0;
      this.beats_in_refresh++;
      if (this.beats_in_refresh >= this.beats_per_refresh) {
        this.do_refresh();
      }
      if (!this.heart_is_beating) {
        this.heart_is_beating = true;
        setTimeout(this.heartbeat, this.beat_tick);
      }
    },
    do_refresh() {
      this.beats_in_refresh = 0;
      this.$store.dispatch("scenarios/loadScenarios", "timer refresh")
    },
    progress_of(completion) {
      if (isNaN(completion)) {
        return undefined;
      }
      let progress = (completion * 100.0).toFixed(2);
      return progress;
    },
    is_running(scenario) {
      return scenario.progress.find(x => {
        return x.state === "Running"
      });
    },
    abbrev(name) {
      return name;
    }
  },
  created() {
    console.log("created component...");
    this.$store.dispatch("scenarios/loadScenarios", "watch panel load");
  },
  mounted() {
    setTimeout(this.heartbeat, 1000);
  }
}

</script>

<style>
</style>

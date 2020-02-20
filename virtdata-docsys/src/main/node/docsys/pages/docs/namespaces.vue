<template>
  <v-app>
    <div>
      <v-list v-model="namespaces">
        <v-list-item v-for="(namespace,idx) in namespaces" :key="idx" :title="namespace.namespace">
          <div display="inline">ns:{{namespace.namespace + ": " + namespace.show}}</div>
          <v-list-item-title>{{namespace.namespace}}</v-list-item-title>
          <v-switch v-model="namespace.show"></v-switch>
          <!--        <v-list-item-action>-->
          <!--          <v-switch v-model="namespace.show"></v-switch>-->
          <!--          &lt;!&ndash;            <v-switch v-model="testval" @change="testval=!testval"></v-switch>&ndash;&gt;-->
          <!--        </v-list-item-action>-->
        </v-list-item>
      </v-list>
      <!--    <v-list :ns_enabled="namespaces.enabled">-->
      <!--      <v-list-group v-for="(nspaths,nsname) in namespaces.enabled">-->
      <!--        <v-list-item>-->
      <!--          <v-list-item-title>{{nsname}}</v-list-item-title>-->
      <!--          <v-list-item-content>{{JSON.stringify(nspaths)}}</v-list-item-content>-->
      <!--        </v-list-item>-->
      <!--      </v-list-group>-->
      <!--      <v-list-group v-for="ns in ns_enabled">-->
      <!--        <v-list-item-title>content</v-list-item-title>-->
      <!--      </v-list-group>-->
      <!--    </v-list>-->
    </div>
    <!--  <v-list>-->
    <!--    <v-list-item><v-label :v-bind="ns"/></v-list-item>-->
    <!--    {{ns}}-->
    <!--  </v-list>-->
  </v-app>
</template>

<script>
    import get_namespaces from '~/mixins/get_namespaces.js';

    export default {
        name: "namespaces",
        mixins: [get_namespaces],
        data(context) {
            // return {
            //     namespaces: []
            // }
        },
        methods: {
            changeTestVal(data) {
                console.log("data:" + data);
                this.testval = !this.testval;
            }
        }
    }
</script>

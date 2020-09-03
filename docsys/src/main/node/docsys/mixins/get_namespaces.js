// // asyncData in multiple mixins seems to be broken, or worse, working as designed
// export default {
//   async asyncData(context) {
//
//     // if (context.req) {
//     //   console.log("avoiding server-side async");
//     //   return;
//     // }
//
//     let baseurl = document.location.href.split('/').slice(0,3).join('/');
//
//     if (context.isDev && baseurl.includes(":3000")) {
//       console.log("Dev mode: remapping 3000 to 12345 for split dev environment.");
//       baseurl = baseurl.replace("3000","12345");
//     }
//
//     let services = baseurl + "/services";
//
//     // console.log("async loading get_categories data: context: " + context);
//     var fm = require('front-matter');
//
//     let namespaces_endpoint = services + "/docs/namespaces";
//
//     let namespaces = await context.$axios.$get(namespaces_endpoint);
//     // let namespaces = await fetch(services+"/docs/namespaces")
//     //   .then(response => {
//     //     return response.json()
//     //   })
//     //   .catch(err => {
//     //     console.log("error:" + err)
//     //   });
//
//     const collated = Array();
//     for (let ena in namespaces) {
//       for (let ns in namespaces[ena]) {
//         collated.push({
//           namespace: ns,
//           show: (ena==="enabled"),
//           paths: namespaces[ena]
//         });
//         console.log ("ns:"+ ns + ", ena: " + ena);
//       }
//       // namespaces[ena].forEach(e => {e.isEnabled = (ena === "enabled")});
//       // collated=collated.concat(namespaces[ena])
//     }
//
//     let result={namespaces: collated};
//     // console.log("namespaces result:"+JSON.stringify(result));
//     return result;
//   }
//
// }

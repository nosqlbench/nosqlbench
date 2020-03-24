// asyncData in multiple mixins seems to be broken, or worse, working as designed
export default {
  async asyncData(context) {

    function fetchStatusHandler(response) {
      if (response.status === 200) {
        return response;
      } else {
        throw new Error(response.statusText);
      }
    }

    if (context.req) {
      console.log("avoiding server-side async");
      return;
    }

    let baseurl = document.location.href.split('/').slice(0,3).join('/');
    if (context.isDev && baseurl.includes(":3000")) {
      console.log("Dev mode: remapping 3000 to 12345 for split dev environment.");
      baseurl = baseurl.replace("3000","12345");
    }

    let services = baseurl + "/services";

    console.log("async loading get_categories data: context: " + context);
    var fm = require('front-matter');


    let paths = await fetch(services+"/docs/markdown.csv")
      .then(res => {
        return res.text()
      })
      .then(body => {
        return body.split("\n")
      })
      .catch(err => {
        console.log("error:" + err)
      });

    let imports = [];
    let promises = [];

    for (let index in paths) {
      let key = paths[index];
      if (key == null || key == "") {
        continue
      }
      const [, name] = key.match(/(.+)\.md$/);
      let detailName = key.split("/").filter(x => x.includes(".md"))[0];
      detailName = detailName.substr(0, detailName.length - 3);

      let categories = key.split("/").filter(x => !x.includes("."))
      //const mdMeta = resolve(key);
      promises.push(fetch(services + "/docs/markdown/" + key)
        .then(res => res.text())
        .then(body => {
          return {
            "rawMD": body,
            "detailName": detailName,
            "categories": categories,
            "name": name
          }
        }));
    }
    var mdData = await Promise.all(
      promises
    );

    for(var data of mdData){

      let rawMD = data.rawMD;

      var mdMeta = fm(rawMD);

      if (mdMeta.attributes == null || mdMeta.attributes.title == null) {
        mdMeta.attributes.title = data.detailName;
      }
      if (typeof mdMeta.attributes.weight === 'undefined') {
        mdMeta.attributes.weight = 0;
      }

      mdMeta.categories = data.categories;
      mdMeta.filename = encodeURIComponent(data.name);

      //console.log("mdMeta:" + JSON.stringify(mdMeta));
      imports.push(mdMeta);
    }
    const categorySet = new Set();
    imports.forEach(x => {
      categorySet.add(x.categories.toString())
    });

    const categories = Array.from(categorySet).map(category => {
        let docs = imports.filter(x => x.categories.toString() === category);
        let summarydoc = docs.find(x => x.filename.endsWith('index'));
        docs = docs.filter(x=>!x.filename.endsWith('index'));
        docs.forEach(d => delete d.body);
        docs.sort((a, b) => a.attributes.weight - b.attributes.weight);
        let weight = summarydoc ? summarydoc.attributes.weight : 0;
        let categoryName = summarydoc ? ( summarydoc.attributes.title ? summarydoc.attributes.title : category ) : category;
        return {category, categoryName, docs, summarydoc, weight}
      }
    ).sort((c1,c2) => c1.weight - c2.weight);

    let active_category='';
    let active_category_name='';
    let active_topic='';

    // IFF no category was active, then make the first category active.
    if (!context.params.slug) {
      console.log("params.slug was not defined");
      active_category=categories[0].category;
      active_category_name=categories[0].categoryName;

      if (categories[0].summarydoc == null && categories[0].docs.length>0) {
          active_topic = categories[0].docs[0].filename;
      }
    } else {
      let parts = context.params.slug.split("/",2);
      active_category=parts[0];
      console.log("==> params.slug[" + context.params.slug + "] active_category[" + active_category + "]");
      active_topic = parts.length>1 ? parts[1] : null;
    }

    if (active_topic !== null && active_topic.endsWith(".html")) {
      active_topic = active_topic.substr(0,active_topic.length-5);
    }
    if (active_topic !== null && active_topic.endsWith(".md")) {
      active_topic = active_topic.substr(0,active_topic.length-3);
    }

    if (active_category !== null && active_category.endsWith(".html")) {
      active_category = active_category.substr(0,active_category.length-5);
    }
    if (active_category !== null && active_category.endsWith(".md")) {
      active_category = active_category.substr(0,active_category.length-3);
    }

    let foundCategory = categories.find(c => c.category === active_category);

    if (foundCategory != undefined){
      active_category_name = categories.find(c => c.category === active_category).categoryName;
    }

    console.log("==> active category[" + active_category + "] topic["+ active_topic +"]");

    // At this point, we have an active category or even a topic.
    // We're all in on loading markdown, but which one?

    let docname = active_category;

    if (active_topic) {
      docname += '/' + active_topic + '.md';
    } else {
      docname += '/' + 'index.md';
    }
    console.log("docname: " + docname);
    var fm = require('front-matter');

    let docbody = "";

    let mdPath = services + '/docs/markdown/' + docname;

    let rawMD = await fetch(services + "/docs/markdown/" + docname)
      .then(fetchStatusHandler)
      .then(res => res.text())
      .then(body => docbody = body)
      .catch(function(error) {
          console.log(error);
      });;


    var markdown = fm(rawMD);

    // console.log("markdown_body:\n" + markdown.body);
    let mydata = {
      markdown_attr: markdown.attributes,
      markdown_body: markdown.body,
      categories: categories,
      active_category: active_category,
      active_category_name : active_category_name,
      active_topic: active_topic
    };
    return mydata;
  }

}

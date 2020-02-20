const Http = new XMLHttpRequest();
const url = 'http://localhost:12345/status.json';
Http.open("GET", url);
Http.send();

Http.onreadystatechange=(e)=>{
    console.log(Http.responseText);
}

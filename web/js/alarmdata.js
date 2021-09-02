let ip_ = ""//"http://192.168.137.159:8090/";
let audioRate = 1;
function init_data() {
    let dataUrl = ip_+"alarm/baseSet";
    let baseData = data_get(dataUrl);

    // let baseData = {"message":"报警基础配置信息","status":200,"data":{"audioRate":1.0,"company":"桐庐红狮"}}.data;
    let companyName = baseData["company"];
    let title = document.getElementById("title");
    let title_div = document.getElementById("title_div");
    title.innerText = companyName + "语音报警模块";
    title_div.innerText = companyName + "语音报警模块";
    audioRate = baseData["audioRate"];

    data_flush();
}

let data_t;
function data_flush(){
    clearInterval(data_t);
    tabledata();
    audio_alarm();
    data_t = setInterval(data_flush,3000);
}

function tabledata() {
    let dataUrl = ip_+"alarm/alarmList";
    let dataObj = data_get(dataUrl);
    // let dataObj = {"data":[
    //     {"level":0,"context":" 煤磨袋收尘压差过高,当前值为-1391.85","value":-1391.845703125,"rate":0.0,"product":"zc","date":"2021-08-13 18:14:34"},
    //         {"level":0,"context":" 1#窑头主蒸汽温度过高,当前值为454.46","value":454.4566650390625,"rate":0.0,"product":"sc","date":"2021-08-13 18:14:34"},
    //         {"level":0,"context":" 除氧器水箱水位过高,当前值为924.91","value":924.908447265625,"rate":0.0,"product":"sc","date":"2021-08-13 18:14:34"},
    //         {"level":0,"context":" 发电机前轴承温度A1过高,当前值为70.38","value":70.37850952148438,"rate":0.0,"product":"sc","date":"2021-08-13 18:14:34"},
    //         {"level":0,"context":" 发电机定子铁芯温度过高,当前值为60.95","value":60.9523811340332,"rate":0.0,"product":"sc","date":"2021-08-13 18:14:34"},
    //         {"level":0,"context":" 发电机定子铁芯温度过高,当前值为60.81","value":60.80586242675781,"rate":0.0,"product":"sc","date":"2021-08-13 18:14:34"},
    //         {"level":0,"context":" 选粉机下部轴承温度过高,当前值为73.63","value":73.62637329101562,"rate":0.0,"product":"sl","date":"2021-08-13 18:14:34"},
    //         {"level":0,"context":" 发电机定子铁芯温度过高,当前值为60.73","value":60.73260116577148,"rate":0.0,"product":"sc","date":"2021-08-13 18:14:34"},
    //         {"level":0,"context":" 选粉机中部轴承温度过高,当前值为78.17","value":78.16850280761719,"rate":0.0,"product":"sl","date":"2021-08-13 18:14:34"}],
    //     "message":"报警列表回去成功","status":200,"size":9}.data;
    let raw_data = []; let pyro_data = []; let cement_data = [];
    for(let i=0;i<dataObj.length;i++){
        let product = dataObj[i]["product"];
        if(product==="sl"){
            raw_data.push(dataObj[i]);
        }
        if(product==="sc"){
            pyro_data.push(dataObj[i]);
        }
        if(product==="zc"){
            cement_data.push(dataObj[i]);
        }
    }
    table_create(raw_data,"raw_body");
    table_create(pyro_data,"pyro_body");
    table_create(cement_data,"cement_body");
}


function data_get(dataUrl) {
    let alarmdata=[];

    $.ajax({
        url:dataUrl,
        type: 'post',
        dataType: 'json',
        async:false,
        success: function (jsonObject) {
            alarmdata = jsonObject.data;
        }
    });
    return alarmdata;
}


function table_create(dataObj,tbodyid) {
    let param_body =document.getElementById(tbodyid);
    dataremove(param_body);

    if(dataObj.length>0){
        for(let i=0;i<dataObj.length;i++){
            let tr=document.createElement('tr');
            let td_level=document.createElement('td');
            let td_content=document.createElement('td');
            let td_curvalue=document.createElement('td');
            let td_chgrate=document.createElement('td');
            let td_almtime=document.createElement('td');
            let td_0=document.createElement('td');

            td_level.setAttribute("class","th1");
            td_content.setAttribute("class","th2");
            td_curvalue.setAttribute("class","th3");
            td_chgrate.setAttribute("class","th4");
            td_almtime.setAttribute("class","th5");

            td_level.innerText = dataObj[i]["level"];
            td_content.innerText = dataObj[i]["context"];
            td_curvalue.innerText = dataObj[i]["value"].toFixed(1);
            td_chgrate.innerText = dataObj[i]["rate"];
            td_almtime.innerText = dataObj[i]["date"];

            tr.appendChild(td_level);
            tr.appendChild(td_content);
            tr.appendChild(td_curvalue);
            tr.appendChild(td_chgrate);
            tr.appendChild(td_almtime);
            tr.appendChild(td_0);
            param_body.appendChild(tr);
        }
    }
}

/**-------创建表格之前，移除body中所有内容------**/
function dataremove(bodyObj){
    if(bodyObj==null)
    {
        console.log("Body of Table not Exist!");
        return;
    }
    for (let i = 0; i < bodyObj.rows.length;)
        bodyObj.deleteRow(i);
}


function audio_alarm() {
    let dataUrl = ip_+"alarm/audioAlarmList";
    let dataObj = data_get(dataUrl);
    // let dataObj = {"data":[],"message":"无报警消息","status":200,"size":0}.data;
    if(dataObj.length>0){
        for(let i=0;i<dataObj.length;i++){
            let content = dataObj[i]["context"];
            doTTS(content);
        }
    }
}


function doTTS(msgtext) {
    console.log("msgtext:",msgtext);
    let msg = new SpeechSynthesisUtterance(msgtext);
    msg.volume = 100;
    msg.rate = audioRate;
    msg.pitch = 1.5;
    window.speechSynthesis.speak(msg);
}


/**---------语音报警测试------------**/
function doTTS1() {
    let msgtext = '桐庐红狮生料莫异常报警！';
    let msg = new SpeechSynthesisUtterance(msgtext);
    console.log("msgtext:",msgtext);
    msg.volume = 100;
    msg.rate = audioRate;
    msg.pitch = 1.5;
    window.speechSynthesis.speak(msg);
}

/**--
 * 获取初始化配置      http://ip:port/alarm/baseSet
 {
    "status":200,
    "message":"",
    "data":[
        {
            "audioRate":1,     语音速度
            "company":"1XXX"   公司名称
        }
    ]

}
 * 获取报警列表      http://ip:port/alarm/alarmList
 * 获取语音报警消息  http://ip:port/alarm/audioAlarmList
 {
    "status":200,
    "message":"",
    "size":1,
    "data":[
    {
        "level":0,
        "context":"1XXX",
        "value":0.1,
        "rate":0.1,
        "product":"sc"，标识工艺(sc 烧成, sl 生料, zc 制成)
        "date":"yyyy-MM-dd HH:mm:ss"
    }
    ]
}
 --**/




# 工程简介
- 宜春 http://192.168.99.213:998/
- 桐庐 http://192.168.137.159/
# 延伸阅读

下步需要做报警和主机设备做关联，比如停机就不进行报警

## 添加报警类型
**接口描述**

* 添加报警类型

**请求 URL**

* /alarmClass/add

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "name": "电器",   
  "code": "dq" 
}
```


<br><br>

## 返回所有报警类型
**接口描述**

* 返回所有报警类型

**请求 URL**

* /alarmClass/get

**请求方式**

* GET

**请求参数**

<br><br>
## 更新报警类型
**接口描述**

* 更新报警类型

**请求 URL**

* /alarmClass/update

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "id": 1213,
  "name": "电器",   
  "code": "dq" 
}
```
<br><br>

## 删除报警类型
**接口描述**

* 删除报警类型

**请求 URL**

* /alarmClass/delete

**请求方式**

* GET

**请求参数**
?id=123 //Long 

<br><br>
## 添加报警历史
**接口描述**

* 添加报警历史

**请求 URL**

* /alarmHistory/add

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "alarmContext": "xxxx",
  "alarmTime": "yyyy-MM-dd HH:mm:ss"
}
```

<br><br>
## 删除报警历史
**接口描述**

* 删除报警历史

**请求 URL**

* /alarmHistory/delete

**请求方式**

* GET

**请求参数**
？id=1212

<br><br>
## 更新报警历史
**接口描述**

* 更新报警历史

**请求 URL**

* /alarmHistory/update

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "id":2123,
  "alarmContext": "xxxx",
  "alarmTime": "yyyy-MM-dd HH:mm:ss"
}
```


<br><br>
## 分页查看报警历史
**接口描述**

* 分页查看报警历史

**请求 URL**

* /alarmHistory/page

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "current": 1,
  "pageSize": 20
}
```

<br><br>

## 分页查看报警历史
**接口描述**

* 分页查看报警历史

**请求 URL**

* /alarmHistory/get/{id}

**请求方式**

* GET（application/json）

**请求参数**
？id=1233

<br><br>

## 添加报警规则
**接口描述**

* 添加报警规则

**请求 URL**

* /alarmRule/add

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "id": 2123,
  "alarmMode":"TRG",
  "alarmSubMode": "RISE",
  "isAudio": true ,
  "larmClassId": 212,
  "pointId": 12,
  "group": "ktj",
  "limiteValue": 12,
  "alarmTemple": "xxxxx"
}
```
<br><br>


## 删除报警规则
**接口描述**

* 删除报警规则

**请求 URL**

* /alarmRule/delete

**请求方式**

* GET

**请求参数**
？id=1213
<br><br>

## 获取报警规则1
**接口描述**

* 获取报警规则(点位id)

**请求 URL**

* /alarmRule/getByPointId

**请求方式**

* GET

**请求参数**
？id=1213
<br><br>



**请求参数**
？id=1213
<br><br>
## 获取报警规则2
**接口描述**

* 获取报警规则(本身id)

**请求 URL**

* /alarmRule/getById

**请求方式**

* GET

**请求参数**
？id=1213
<br><br>


## 添加报警规则开关映射
**接口描述**

* 添加报警规则开关映射

**请求 URL**

* /alarmRuleSwitchMap/add

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "refAlarmRuleId": 21213,
 "refSwitchId":1213 
}
```
<br><br>

## 更新报警规则开关映射
**接口描述**

* 更新报警规则开关映射

**请求 URL**

* /alarmRuleSwitchMap/update

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "refAlarmRuleId": 21213,
 "refSwitchId":1213 
}
```
<br><br>

## 添加设备
**接口描述**

* 添加设备

**请求 URL**

* /device/add

**请求方式**

* POST（application/json）

**请求参数**

```json
{
    "deviceNo": "blj",
    "deviceName":"篦冷机"
}
```
<br><br>

## 删除设备
**接口描述**

* 删除设备

**请求 URL**

* /device/add

**请求方式**

* GET

**请求参数**
id=212
<br><br>

## 更新设备
**接口描述**

* 更新设备

**请求 URL**

* /device/update

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "id": 123,
    "deviceNo": "blj",
    "deviceName":"篦冷机"
}
```
<br><br>

## 获取设备
**接口描述**

* 获取设备

**请求 URL**

* /device/get

**请求方式**

* GET（application/json）

**请求参数**
无
<br><br>

## 点位添加
**接口描述**

* 点位添加

**请求 URL**

* /point/add

**请求方式**

* POST（application/json）

**请求参数**

```json
{
   "tag": "xx.pv",
  "name":"篦冷机压力",
  "refDeviceId": 1213
}
```
<br><br>


## 点位添加
**接口描述**

* 点位添加

**请求 URL**

* /point/add

**请求方式**

* POST（application/json）

**请求参数**

```json
{
   "tag": "xx.pv",
  "name":"篦冷机压力",
  "refDeviceId": 1213
}
```
<br><br>



## 点位删除
**接口描述**

* 点位删除

**请求 URL**

* /point/delete

**请求方式**

* GET

**请求参数**
id=123
<br><br>


## 点位信息更新
**接口描述**

* 点位添加

**请求 URL**

* /point/update

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "id": 1213,
   "tag": "xx.pv",
  "name":"篦冷机压力",
  "refDeviceId": 1213
}
```
<br><br>


## 获取点位信息
**接口描述**

* 获取点位信息(通过deviceId)

**请求 URL**

* /point/get

**请求方式**

* POST（application/json）

**请求参数**
id=1212
<br><br>


## 添加规则开关
**接口描述**

* 添加规则开关

**请求 URL**

* /switch/add

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "name":"篦冷机点位报警开关"
}
```
<br><br>

## 删除规则开关
**接口描述**

* 删除规则开关

**请求 URL**

* /switch/delete

**请求方式**

* GET

**请求参数**
id=123
<br><br>


## 更新规则开关
**接口描述**

* 更新规则开关

**请求 URL**

* /switch/update

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "id": 123,
  "name":"篦冷机点位报警开关"
}
```
<br><br>


## 获取规则开关
**接口描述**

* 获取规则开关

**请求 URL**

* /switch/get

**请求方式**

* GET（application/json）

**请求参数**
无
<br><br>



## 添加开关规则
**接口描述**

* 添加开关规则

**请求 URL**

* /switchRule/add

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "refSwitchId":123,
  "ruleCode":"ge",
  "pointId":123,
  "limitValue":20 
}
```
<br><br>



## 更新开关规则
**接口描述**

* 更新开关规则

**请求 URL**

* /switchRule/update

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "id": 123,
  "refSwitchId":123,
  "ruleCode":"ge",
  "pointId":123,
  "limitValue":20 
}
```
<br><br>



## 删除开关规则
**接口描述**

* 删除开关规则

**请求 URL**

* /switchRule/delete

**请求方式**

* GET

**请求参数**
id=213
<br><br>

## 获取开关规则
**接口描述**

* 获取开关规则

**请求 URL**

* /switchRule/get

**请求方式**

* GET

**请求参数**
无
<br><br>





## 添加系统设置
**接口描述**

* 添加系统设置

**请求 URL**

* /systemConfig/add

**请求方式**

* POST（application/json）

**请求参数**

```json
{
    "name": "语音速度设置",
    "code": "ration",
    "value" :"/api/xxx",
    "group": "collect"
}
```
<br><br>


## 更新系统设置
**接口描述**

* 更新系统设置

**请求 URL**

* /systemConfig/update

**请求方式**

* POST（application/json）

**请求参数**

```json
{
  "id": 123,
    "name": "语音速度设置",
    "code": "ration",
    "value" :"/api/xxx",
    "group": "collect"
}
```
<br><br>




## 删除系统设置
**接口描述**

* 删除系统设置

**请求 URL**

* /systemConfig/delete

**请求方式**

* GET

**请求参数**
id=213
<br><br>

## 获取系统设置
**接口描述**

* 获取系统设置

**请求 URL**

* /systemConfig/get

**请求方式**

* GET

**请求参数**
无
<br><br>


## 获取实时报警列表
**接口描述**

* 获取实时报警列表

**请求 URL**

* /alarm/alarmList

**请求方式**

* GET

**请求参数**
无
**返回参数**
{"data":[
        {
            "level":0,
            "context":"123",//报警内容
            "value":123,//当前值
             "rate":123,//变化率
            "product":"sl",//工序
            "date":"yyyy-MM-dd HH:mm:ss",
            "alarmId":1231,//用于识别同一点位名称的不同类型报警类型
            "alarmHistoryId":123, //历史报警id
            "pushStatus":1//1,"报警生成",2,"设备语音报警",3,"推送设备健康"
        }
],
"message":"1213",
 "status":200,
"size":1,
}

<br><br>


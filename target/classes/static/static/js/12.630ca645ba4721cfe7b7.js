(window.webpackJsonp=window.webpackJsonp||[]).push([[12],{"3uy1":function(t,e,r){"use strict";r.d(e,"m",function(){return d}),r.d(e,"k",function(){return l}),r.d(e,"a",function(){return s}),r.d(e,"g",function(){return c}),r.d(e,"d",function(){return u}),r.d(e,"l",function(){return m}),r.d(e,"o",function(){return f}),r.d(e,"b",function(){return p}),r.d(e,"h",function(){return h}),r.d(e,"e",function(){return g}),r.d(e,"n",function(){return v}),r.d(e,"p",function(){return b}),r.d(e,"j",function(){return y}),r.d(e,"c",function(){return F}),r.d(e,"i",function(){return L}),r.d(e,"f",function(){return D});var a=r("t3Un"),n=r("f0Pt"),o=r.n(n),i="http://localhost:80/system";function d(t){return Object(a.a)({url:i+"/vendingDistrict/list",method:"get",params:t})}function l(t){return Object(a.a)({url:i+"/corp/vaguelist",method:"get",params:t})}var s=function(t){return o.a.post(i+"/vendingDistrict/add",t).then(function(t){return t.data})},c=function(t){return o.a.post(i+"/vendingDistrict/edit",t).then(function(t){return t.data})},u=function(t){return o.a.post(i+"/vendingDistrict/remove",t).then(function(t){return t.data})};function m(t){return Object(a.a)({url:i+"/vendingLine/list",method:"get",params:t})}function f(t){return Object(a.a)({url:i+"/vendingDistrict/listAll",method:"get",params:t})}var p=function(t){return o.a.post(i+"/vendingLine/add",t).then(function(t){return t.data})},h=function(t){return o.a.post(i+"/vendingLine/edit",t).then(function(t){return t.data})},g=function(t){return o.a.post(i+"/vendingLine/remove",t).then(function(t){return t.data})};function v(t){return Object(a.a)({url:i+"/vendingPoint/list",method:"get",params:t})}function b(t){return Object(a.a)({url:i+"/vendingLine/listAll",method:"get",params:t})}function y(t){return Object(a.a)({url:i+"/dispatch/getChild",method:"get",params:t})}var F=function(t){return o.a.post(i+"/vendingPoint/add",t).then(function(t){return t.data})},L=function(t){return o.a.post(i+"/vendingPoint/edit",t).then(function(t){return t.data})},D=function(t){return o.a.post(i+"/vendingPoint/remove",t).then(function(t){return t.data})}},"5OhU":function(t,e,r){"use strict";r.r(e);var a=r("6ZY3"),n=r.n(a),o=r("3uy1"),i=r("OLzz"),d=r.n(i),l={data:function(){return{cityId:"0",corpName:"",lineList:[],regionList:[],restaurants:[],cityList:{countryData:[],ProvinceData:[],cityData:[],DistrictData:[]},companyName:"",timeout:null,formLabelWidth:"120px",addForm:{districtId:"",lineId:"",code:"",name:"",country:"",province:"",city:"",district:"",adderss:"",longitude:"",latitude:"",corpId:""},Rules:{code:[{required:!0,message:" ",trigger:"blur"}],name:[{required:!0,message:" ",trigger:"blur"}],lineId:[{required:!0,message:" ",trigger:"blur"}],corpName:[{required:!0,validator:function(t,e,r){if(!e)return r(new Error(" "));r()},trigger:"change"}],country:[{required:!0,message:" ",trigger:"blur"}],adderss:[{required:!0,message:" ",trigger:"blur"}]}}},watch:{},methods:{ReturnLevel:function(){this.$router.push({path:"/pointposition/position"})},addSubmitForm:function(t){var e=this;this.$refs[t].validate(function(t){if(!t)return console.log("error submit!!"),!1;e.$confirm("确认提交吗？","提示",{}).then(function(){d.a.start();var t=n()({},e.addForm);Object(o.c)(t).then(function(t){"0000"==t.zhead.reTCode?(d.a.done(),e.$notify({title:"成功",message:t.zhead.retMsg,type:"success"}),e.$refs.addForm.resetFields(),e.$router.push({path:"/pointposition/position"})):e.$notify({title:"失败",message:t.zhead.retMsg,type:"error"})})})})},resetForm:function(t){this.$refs[t].resetFields()},getData:function(){var t=this,e=this;this.listLoading=!0;Object(o.o)({}).then(function(r){"0000"===r.zhead.reTCode?(e.regionList=r.zbody.datas.rows,console.log("所有区域",e.regionList)):t.$notify({title:"失败",message:res.zhead.retMsg,type:"error"})})},getLine:function(){var t=this,e=this;this.listLoading=!0;Object(o.p)({}).then(function(r){"0000"===r.zhead.reTCode?(e.lineList=r.zbody.datas.rows,console.log("所有线路",e.lineList)):t.$notify({title:"失败",message:res.zhead.retMsg,type:"error"})})},querySearchAsync:function(t,e){var r=this.restaurants,a=t?r.filter(this.createStateFilter(t)):r,n=this,i={corpName:n.companyName};Object(o.k)(i).then(function(t){n.restaurants=t.zbody.datas,e(a)})},createStateFilter:function(t){return function(e){return 0===e.corpName.toLowerCase().indexOf(t.toLowerCase())}},handleSelect:function(t){this.addForm.corpId=t.corpId,this.addForm.corpName=t.corpName,console.log(t)},getChildData:function(t){var e=this,r=this,a="0";"1"==t?a=r.addForm.country:"2"==t?a=r.addForm.province:"3"==t&&(a=r.addForm.city);var n={parentId:a};Object(o.j)(n).then(function(a){"0000"===a.zhead.reTCode?("1"==t?r.cityList.ProvinceData=a.zbody.datas.rows:"2"==t?r.cityList.cityData=a.zbody.datas.rows:"3"==t?r.cityList.DistrictData=a.zbody.datas.rows:r.cityList.countryData=a.zbody.datas.rows,console.log("行政区111",r.cityList)):e.$notify({title:"失败",message:res.zhead.retMsg,type:"error"})})}},mounted:function(){this.getData(),this.getLine(),this.getChildData()},components:{}},s=(r("bX1/"),r("mDrr")),c=Object(s.a)(l,function(){var t=this,e=t.$createElement,r=t._self._c||e;return r("div",{staticClass:"div-warp"},[r("el-card",{staticClass:"box-card"},[r("div",{staticClass:"clearfix",attrs:{slot:"header"},slot:"header"},[r("span",[t._v("新增点位")])]),t._v(" "),r("el-form",{ref:"addForm",staticClass:"formWidth",attrs:{model:t.addForm,rules:t.Rules},on:{submit:function(e){return e.preventDefault(),t.onSubmit(e)}}},[r("el-form-item",{attrs:{label:"点位编号",prop:"code",size:"small","label-width":t.formLabelWidth}},[r("el-input",{attrs:{"auto-complete":"off"},model:{value:t.addForm.code,callback:function(e){t.$set(t.addForm,"code",e)},expression:"addForm.code"}})],1),t._v(" "),r("el-form-item",{attrs:{label:"点位名称",prop:"name",size:"small","label-width":t.formLabelWidth}},[r("el-input",{attrs:{"auto-complete":"off"},model:{value:t.addForm.name,callback:function(e){t.$set(t.addForm,"name",e)},expression:"addForm.name"}})],1),t._v(" "),r("el-form-item",{attrs:{label:"线路名称",prop:"lineId",size:"small","label-width":t.formLabelWidth}},[r("el-select",{attrs:{"auto-complete":"off",placeholder:"请选择"},model:{value:t.addForm.lineId,callback:function(e){t.$set(t.addForm,"lineId",e)},expression:"addForm.lineId"}},t._l(t.lineList,function(t){return r("el-option",{key:t.logid,attrs:{label:t.name,value:t.logid}})}))],1),t._v(" "),r("el-form-item",{attrs:{label:"所属商户",prop:"corpName",size:"small","label-width":t.formLabelWidth}},[r("el-autocomplete",{attrs:{"fetch-suggestions":t.querySearchAsync,"value-key":"corpName",placeholder:"输入搜索公司"},on:{select:t.handleSelect},model:{value:t.addForm.corpName,callback:function(e){t.$set(t.addForm,"corpName",e)},expression:"addForm.corpName"}})],1),t._v(" "),r("el-form-item",{attrs:{label:"所属行政区域",prop:"country",size:"small","label-width":t.formLabelWidth}},[r("el-select",{attrs:{placeholder:"请选择国家"},on:{change:function(e){t.getChildData("1")}},model:{value:t.addForm.country,callback:function(e){t.$set(t.addForm,"country",e)},expression:"addForm.country"}},t._l(t.cityList.countryData,function(t){return r("el-option",{key:t.logid,attrs:{label:t.name,value:t.logid}})})),t._v(" "),r("el-select",{attrs:{placeholder:"请选择省份或州"},on:{change:function(e){t.getChildData("2")}},model:{value:t.addForm.province,callback:function(e){t.$set(t.addForm,"province",e)},expression:"addForm.province"}},t._l(t.cityList.ProvinceData,function(t){return r("el-option",{key:t.logid,attrs:{label:t.name,value:t.logid}})})),t._v(" "),r("el-select",{attrs:{placeholder:"请选择城市"},on:{change:function(e){t.getChildData("3")}},model:{value:t.addForm.city,callback:function(e){t.$set(t.addForm,"city",e)},expression:"addForm.city"}},t._l(t.cityList.cityData,function(t){return r("el-option",{key:t.logid,attrs:{label:t.name,value:t.logid}})})),t._v(" "),r("el-select",{attrs:{placeholder:"请选择区"},model:{value:t.addForm.district,callback:function(e){t.$set(t.addForm,"district",e)},expression:"addForm.district"}},t._l(t.cityList.DistrictData,function(t){return r("el-option",{key:t.logid,attrs:{label:t.name,value:t.logid}})}))],1),t._v(" "),r("el-form-item",{attrs:{label:"详细地址",prop:"adderss",size:"small","label-width":t.formLabelWidth}},[r("el-input",{attrs:{"auto-complete":"off"},model:{value:t.addForm.adderss,callback:function(e){t.$set(t.addForm,"adderss",e)},expression:"addForm.adderss"}})],1),t._v(" "),r("el-form-item",{attrs:{label:"经度",prop:"longitude",size:"small","label-width":t.formLabelWidth}},[r("el-input",{attrs:{"auto-complete":"off"},model:{value:t.addForm.longitude,callback:function(e){t.$set(t.addForm,"longitude",e)},expression:"addForm.longitude"}})],1),t._v(" "),r("el-form-item",{attrs:{label:"维度",prop:"latitude",size:"small","label-width":t.formLabelWidth}},[r("el-input",{attrs:{"auto-complete":"off"},model:{value:t.addForm.latitude,callback:function(e){t.$set(t.addForm,"latitude",e)},expression:"addForm.latitude"}})],1),t._v(" "),r("el-form-item",{attrs:{label:"描述",prop:"remark",size:"small","label-width":t.formLabelWidth}},[r("el-input",{attrs:{type:"textarea"},model:{value:t.addForm.remark,callback:function(e){t.$set(t.addForm,"remark",e)},expression:"addForm.remark"}})],1)],1),t._v(" "),r("div",{staticClass:"dialog-footer"},[r("el-button",{on:{click:t.ReturnLevel}},[t._v("取 消")]),t._v(" "),r("el-button",{attrs:{type:"primary"},on:{click:function(e){t.addSubmitForm("addForm")}}},[t._v("确 定")])],1)],1)],1)},[],!1,null,"762a63e2",null);c.options.__file="add.vue";e.default=c.exports},ZGiU:function(t,e,r){},"bX1/":function(t,e,r){"use strict";var a=r("ysO6");r.n(a).a},ysO6:function(t,e,r){var a=r("ZGiU");"string"==typeof a&&(a=[[t.i,a,""]]),a.locals&&(t.exports=a.locals);(0,r("hctG").default)("335825b1",a,!0,{})}}]);
//# sourceMappingURL=12.630ca645ba4721cfe7b7.js.map
(window.webpackJsonp=window.webpackJsonp||[]).push([[16],{"++Jm":function(e,t,a){"use strict";var o=a("PHDR");a.n(o).a},"9WTv":function(e,t,a){"use strict";a.d(t,"a",function(){return l}),a.d(t,"c",function(){return n}),a.d(t,"f",function(){return d}),a.d(t,"d",function(){return c}),a.d(t,"e",function(){return m}),a.d(t,"g",function(){return u}),a.d(t,"b",function(){return p});var o=a("t3Un"),r=a("f0Pt"),i=a.n(r),s="http://localhost:80/system",l=function(e){return i.a.post(s+"/vending/add",e).then(function(e){return e.data})};function n(e){return Object(o.a)({url:s+"/dict/data/list",method:"get",params:e})}function d(e){return Object(o.a)({url:s+"/vendingModel/list",method:"get",params:e})}function c(e){return Object(o.a)({url:s+"/vendingPoint/list",method:"get",params:e})}function m(e){return Object(o.a)({url:s+"/vending/selectNVendingList",method:"get",params:e})}function u(e){return Object(o.a)({url:s+"/vendingModel/get",method:"get",params:e})}function p(e){return Object(o.a)({url:s+"/productInfo/list",method:"get",params:e})}},PHDR:function(e,t,a){var o=a("ym0W");"string"==typeof o&&(o=[[e.i,o,""]]),o.locals&&(e.exports=o.locals);(0,a("hctG").default)("63ecd01e",o,!0,{})},YPsx:function(e,t,a){"use strict";a.r(t);var o=a("6ZY3"),r=a.n(o),i=a("9WTv"),s=a("OLzz"),l=a.n(s),n={data:function(){return{ManufactorOptions:[],CabinetOptions:[],stateOptions:[],networkOptions:[],modelOptions:[],options:[],palychek:[],restaurants:[],companyName:"",value:"",input:"",formLabelWidth:"120px",addForm:{siteCode:"",siteName:"",factoryId:"",cabinetType:"",deviceId:"",districtId:"",lineId:"",pointId:"",pointName:"",payType:"",mediaType:"1",netWork:"",sellState:"",description:""},Rules:{siteCode:[{required:!0,message:" ",trigger:"change"}],siteName:[{required:!0,message:" ",trigger:"change"}],dictLabel:[{required:!0,message:" ",trigger:"change"}],factoryId:[{required:!0,message:" ",trigger:"change"}],cabinetType:[{required:!0,message:" ",trigger:"change"}],netWork:[{required:!0,message:" ",trigger:"change"}],sellState:[{required:!0,message:" ",trigger:"change"}],deviceId:[{required:!0,message:" ",trigger:"change"}],pointId:[{required:!0,message:" ",trigger:"change"}]}}},watch:{},methods:{handleCheckedCitiesChange:function(e){this.addForm.payType=e.join(",")},addSubmitForm:function(e){var t=this;this.$refs[e].validate(function(e){if(!e)return console.log("error submit!!"),!1;t.$confirm("确认提交吗？","提示",{}).then(function(){l.a.start();var e=r()({},t.addForm);Object(i.a)(e).then(function(e){"0000"==e.zhead.reTCode?(l.a.done(),t.$notify({title:"成功",message:e.zhead.retMsg,type:"success"}),t.$refs.addForm.resetFields(),t.$router.push({path:"/equipment/machine/index"})):t.$notify({title:"失败",message:e.zhead.retMsg,type:"error"})})})})},resetForm:function(e){this.$refs[e].resetFields()},getMachineSelectList:function(e){var t=this,a=this,o={dictType:e};Object(i.c)(o).then(function(o){"0000"===o.zhead.reTCode?(l.a.done(),"sys_factory"===e?(a.ManufactorOptions=o.zbody.datas.rows,console.log("厂家列表：：",a.ManufactorOptions)):"sys_cabinet_type"===e?(a.CabinetOptions=o.zbody.datas.rows,console.log("货柜类型：：",a.CabinetOptions)):"sys_net_work"===e?(a.networkOptions=o.zbody.datas.rows,console.log("网络类型：：",a.networkOptions)):"sys_sell_state"===e&&(a.stateOptions=o.zbody.datas.rows,console.log("售卖状态：：",a.stateOptions))):t.$notify({title:"失败",message:res.zhead.retMsg,type:"error"})})},getModel:function(){var e=this,t=this;Object(i.f)({}).then(function(a){"0000"===a.zhead.reTCode?(t.modelOptions=a.zbody.datas.rows,console.log("机型：：",a.zbody.datas.rows),l.a.done()):e.$notify({title:"失败",message:res.zhead.retMsg,type:"error"})})},querySearchAsync:function(e,t){var a=this.restaurants,o=e?a.filter(this.createStateFilter(e)):a,r=this,s={name:r.companyName};Object(i.d)(s).then(function(e){r.restaurants=e.zbody.datas.rows,console.log(r.restaurants),t(o)})},createStateFilter:function(e){return function(t){return 0===t.name.toLowerCase().indexOf(e.toLowerCase())}},handleSelect:function(e){this.addForm.pointId=e.pointId,this.addForm.pointName=e.name,console.log(e.pointId)}},mounted:function(){this.getMachineSelectList("sys_factory"),this.getMachineSelectList("sys_cabinet_type"),this.getMachineSelectList("sys_net_work"),this.getMachineSelectList("sys_sell_state"),this.getModel()},components:{}},d=(a("++Jm"),a("mDrr")),c=Object(d.a)(n,function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticClass:"div-warp"},[a("el-card",{staticClass:"box-card"},[a("div",{staticClass:"clearfix",attrs:{slot:"header"},slot:"header"},[a("span",[e._v("编辑售货机")])]),e._v(" "),a("el-form",{ref:"addForm",staticClass:"formWidth",attrs:{model:e.addForm,rules:e.Rules},on:{submit:function(t){return t.preventDefault(),e.onSubmit(t)}}},[a("el-form-item",{attrs:{label:"售货机编号",prop:"siteCode",size:"small","label-width":e.formLabelWidth}},[a("el-input",{model:{value:e.addForm.siteCode,callback:function(t){e.$set(e.addForm,"siteCode",t)},expression:"addForm.siteCode"}})],1),e._v(" "),a("el-form-item",{attrs:{label:"售货机名称",prop:"siteName",size:"small","label-width":e.formLabelWidth}},[a("el-input",{attrs:{"auto-complete":"off"},model:{value:e.addForm.siteName,callback:function(t){e.$set(e.addForm,"siteName",t)},expression:"addForm.siteName"}})],1),e._v(" "),a("el-form-item",{attrs:{label:"厂家",prop:"factoryId",size:"small","label-width":e.formLabelWidth}},[a("el-select",{attrs:{size:"small",placeholder:"请选择厂家"},model:{value:e.addForm.factoryId,callback:function(t){e.$set(e.addForm,"factoryId",t)},expression:"addForm.factoryId"}},e._l(e.ManufactorOptions,function(e){return a("el-option",{key:e.dictCode,attrs:{label:e.dictLabel,value:e.dictCode}})}))],1),e._v(" "),a("el-form-item",{attrs:{label:"货柜类型",prop:"cabinetType",size:"small","label-width":e.formLabelWidth}},[a("el-select",{attrs:{size:"small",placeholder:"请选择负责人姓名"},model:{value:e.addForm.cabinetType,callback:function(t){e.$set(e.addForm,"cabinetType",t)},expression:"addForm.cabinetType"}},e._l(e.CabinetOptions,function(e){return a("el-option",{key:e.dictCode,attrs:{label:e.dictLabel,value:e.dictCode}})}))],1),e._v(" "),a("el-form-item",{attrs:{label:"机型",prop:"deviceId",size:"small","label-width":e.formLabelWidth}},[a("el-select",{attrs:{size:"small",placeholder:"请选择负责人姓名"},model:{value:e.addForm.deviceId,callback:function(t){e.$set(e.addForm,"deviceId",t)},expression:"addForm.deviceId"}},e._l(e.modelOptions,function(e){return a("el-option",{key:e.deviceId,attrs:{label:e.deviceId,value:e.deviceId}})}))],1),e._v(" "),a("el-form-item",{attrs:{label:"点位",prop:"pointId",size:"small","label-width":e.formLabelWidth}},[a("el-autocomplete",{attrs:{"fetch-suggestions":e.querySearchAsync,"value-key":"name",placeholder:"输入搜索点位"},on:{select:e.handleSelect},model:{value:e.addForm.name,callback:function(t){e.$set(e.addForm,"name",t)},expression:"addForm.name"}})],1),e._v(" "),a("el-form-item",{attrs:{label:"网络类型",prop:"netWork",size:"small","label-width":e.formLabelWidth}},[a("el-select",{attrs:{size:"small",placeholder:"请选择负责人姓名"},model:{value:e.addForm.netWork,callback:function(t){e.$set(e.addForm,"netWork",t)},expression:"addForm.netWork"}},e._l(e.networkOptions,function(e){return a("el-option",{key:e.dictValue,attrs:{label:e.dictLabel,value:e.dictValue}})}))],1),e._v(" "),a("el-form-item",{attrs:{label:"售卖状态",prop:"sellState",size:"small","label-width":e.formLabelWidth}},[a("el-select",{attrs:{size:"small",placeholder:"请选择负责人姓名"},model:{value:e.addForm.sellState,callback:function(t){e.$set(e.addForm,"sellState",t)},expression:"addForm.sellState"}},e._l(e.stateOptions,function(e){return a("el-option",{key:e.dictValue,attrs:{label:e.dictLabel,value:e.dictValue}})}))],1),e._v(" "),a("el-form-item",{attrs:{label:"支付方式",size:"small","label-width":e.formLabelWidth}},[a("el-checkbox-group",{on:{change:e.handleCheckedCitiesChange},model:{value:e.palychek,callback:function(t){e.palychek=t},expression:"palychek"}},[a("el-checkbox",{attrs:{label:"1"}},[e._v("支付宝")]),e._v(" "),a("el-checkbox",{attrs:{label:"2"}},[e._v("微信")]),e._v(" "),a("el-checkbox",{attrs:{label:"3"}},[e._v("现金")])],1)],1),e._v(" "),a("el-form-item",{attrs:{label:"视频播放",size:"small","label-width":e.formLabelWidth}},[a("el-radio-group",{attrs:{size:"mini"},model:{value:e.addForm.mediaType,callback:function(t){e.$set(e.addForm,"mediaType",t)},expression:"addForm.mediaType"}},[a("el-radio",{attrs:{label:"1",border:""}},[e._v("支持")]),e._v(" "),a("el-radio",{attrs:{label:"2",border:""}},[e._v("不支持")])],1)],1)],1),e._v(" "),a("div",{staticClass:"bntBox"},[a("el-button",{on:{click:function(t){e.dialogAddTableVisible=!1}}},[e._v("取 消")]),e._v(" "),a("el-button",{attrs:{type:"primary"},on:{click:function(t){e.addSubmitForm("addForm")}}},[e._v("提交")])],1)],1)],1)},[],!1,null,"40c8f0ae",null);c.options.__file="edit.vue";t.default=c.exports},ym0W:function(e,t,a){}}]);
//# sourceMappingURL=16.170049c52cc1f895df7b.js.map
(window.webpackJsonp=window.webpackJsonp||[]).push([[6],{"1wag":function(t,e,n){},"3uy1":function(t,e,n){"use strict";n.d(e,"m",function(){return d}),n.d(e,"k",function(){return u}),n.d(e,"a",function(){return s}),n.d(e,"g",function(){return c}),n.d(e,"d",function(){return l}),n.d(e,"l",function(){return m}),n.d(e,"o",function(){return f}),n.d(e,"b",function(){return p}),n.d(e,"h",function(){return h}),n.d(e,"e",function(){return v}),n.d(e,"n",function(){return b}),n.d(e,"p",function(){return g}),n.d(e,"j",function(){return F}),n.d(e,"c",function(){return w}),n.d(e,"i",function(){return y}),n.d(e,"f",function(){return L});var r=n("t3Un"),a=n("f0Pt"),o=n.n(a),i="http://localhost:80/system";function d(t){return Object(r.a)({url:i+"/vendingDistrict/list",method:"get",params:t})}function u(t){return Object(r.a)({url:i+"/corp/vaguelist",method:"get",params:t})}var s=function(t){return o.a.post(i+"/vendingDistrict/add",t).then(function(t){return t.data})},c=function(t){return o.a.post(i+"/vendingDistrict/edit",t).then(function(t){return t.data})},l=function(t){return o.a.post(i+"/vendingDistrict/remove",t).then(function(t){return t.data})};function m(t){return Object(r.a)({url:i+"/vendingLine/list",method:"get",params:t})}function f(t){return Object(r.a)({url:i+"/vendingDistrict/listAll",method:"get",params:t})}var p=function(t){return o.a.post(i+"/vendingLine/add",t).then(function(t){return t.data})},h=function(t){return o.a.post(i+"/vendingLine/edit",t).then(function(t){return t.data})},v=function(t){return o.a.post(i+"/vendingLine/remove",t).then(function(t){return t.data})};function b(t){return Object(r.a)({url:i+"/vendingPoint/list",method:"get",params:t})}function g(t){return Object(r.a)({url:i+"/vendingLine/listAll",method:"get",params:t})}function F(t){return Object(r.a)({url:i+"/dispatch/getChild",method:"get",params:t})}var w=function(t){return o.a.post(i+"/vendingPoint/add",t).then(function(t){return t.data})},y=function(t){return o.a.post(i+"/vendingPoint/edit",t).then(function(t){return t.data})},L=function(t){return o.a.post(i+"/vendingPoint/remove",t).then(function(t){return t.data})}},"9j94":function(t,e,n){"use strict";n.r(e);var r=n("6ZY3"),a=n.n(r),o=n("3uy1"),i=n("OLzz"),d=n.n(i),u={data:function(){return{restaurants:[],companyName:"",timeout:null,formLabelWidth:"120px",formInputWidth:"200px;",addForm:{name:"",code:"",manager:"",longitude:"",latitude:"",mobile:"",description:"",corpId:"",corpName:""},Rules:{code:[{required:!0,message:" ",trigger:"blur"}],name:[{required:!0,message:" ",trigger:"change"}],corpName:[{required:!0,validator:function(t,e,n){if(!e)return n(new Error(" "));n()},trigger:"change"}]}}},watch:{},methods:{ReturnLevel:function(){this.$router.push({path:"/pointposition/area"})},addSubmitForm:function(t){var e=this;this.$refs[t].validate(function(t){if(!t)return console.log("error submit!!"),!1;e.$confirm("确认提交吗？","提示",{}).then(function(){d.a.start();var t=a()({},e.addForm);Object(o.a)(t).then(function(t){"0000"==t.zhead.reTCode?(d.a.done(),e.$notify({title:"成功",message:t.zhead.retMsg,type:"success"}),e.$refs.addForm.resetFields(),e.$router.push({path:"/pointposition/area"})):e.$notify({title:"失败",message:t.zhead.retMsg,type:"error"})})})})},resetForm:function(t){this.$refs[t].resetFields()},querySearchAsync:function(t,e){var n=this.restaurants,r=t?n.filter(this.createStateFilter(t)):n,a=this,i={corpName:a.companyName};Object(o.k)(i).then(function(t){a.restaurants=t.zbody.datas,e(r)})},createStateFilter:function(t){return function(e){return 0===e.corpName.toLowerCase().indexOf(t.toLowerCase())}},handleSelect:function(t){this.addForm.corpId=t.corpId,this.addForm.corpName=t.corpName,console.log(t)}},mounted:function(){},components:{}},s=(n("B9jo"),n("mDrr")),c=Object(s.a)(u,function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"div-warp"},[n("el-card",{staticClass:"box-card"},[n("div",{staticClass:"clearfix",attrs:{slot:"header"},slot:"header"},[n("span",[t._v("新增区域")])]),t._v(" "),n("el-form",{ref:"addForm",staticClass:"formWidth",attrs:{model:t.addForm,rules:t.Rules},on:{submit:function(e){return e.preventDefault(),t.onSubmit(e)}}},[n("el-form-item",{attrs:{label:"区域编号",prop:"code",size:"small","label-width":t.formLabelWidth}},[n("el-input",{attrs:{"auto-complete":"off"},model:{value:t.addForm.code,callback:function(e){t.$set(t.addForm,"code",e)},expression:"addForm.code"}})],1),t._v(" "),n("el-form-item",{attrs:{label:"区域名称",prop:"name",size:"small","label-width":t.formLabelWidth}},[n("el-input",{attrs:{"auto-complete":"off"},model:{value:t.addForm.name,callback:function(e){t.$set(t.addForm,"name",e)},expression:"addForm.name"}})],1),t._v(" "),n("el-form-item",{attrs:{label:"负责人姓名",prop:"manager",size:"small","label-width":t.formLabelWidth}},[n("el-input",{attrs:{"auto-complete":"off"},model:{value:t.addForm.manager,callback:function(e){t.$set(t.addForm,"manager",e)},expression:"addForm.manager"}})],1),t._v(" "),n("el-form-item",{attrs:{label:"联系方式",size:"small","label-width":t.formLabelWidth}},[n("el-input",{attrs:{"auto-complete":"off"},model:{value:t.addForm.mobile,callback:function(e){t.$set(t.addForm,"mobile",e)},expression:"addForm.mobile"}})],1),t._v(" "),n("el-form-item",{attrs:{label:"商户名称",prop:"corpName",size:"small","label-width":t.formLabelWidth}},[n("el-autocomplete",{attrs:{"fetch-suggestions":t.querySearchAsync,"value-key":"corpName",placeholder:"输入搜索公司"},on:{select:t.handleSelect},model:{value:t.addForm.corpName,callback:function(e){t.$set(t.addForm,"corpName",e)},expression:"addForm.corpName"}})],1),t._v(" "),n("el-form-item",{attrs:{label:"描述",size:"small","label-width":t.formLabelWidth}},[n("el-input",{attrs:{type:"textarea"},model:{value:t.addForm.description,callback:function(e){t.$set(t.addForm,"description",e)},expression:"addForm.description"}})],1)],1),t._v(" "),n("div",{staticClass:"dialog-footer"},[n("el-button",{on:{click:t.ReturnLevel}},[t._v("取 消")]),t._v(" "),n("el-button",{attrs:{type:"primary"},on:{click:function(e){t.addSubmitForm("addForm")}}},[t._v("确 定")])],1)],1)],1)},[],!1,null,"18ba67f2",null);c.options.__file="add.vue";e.default=c.exports},B9jo:function(t,e,n){"use strict";var r=n("IRWS");n.n(r).a},IRWS:function(t,e,n){var r=n("1wag");"string"==typeof r&&(r=[[t.i,r,""]]),r.locals&&(t.exports=r.locals);(0,n("hctG").default)("1682613a",r,!0,{})}}]);
//# sourceMappingURL=6.22cec38d039ec58d5ade.js.map
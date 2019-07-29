///**
//* Epub3.0 NoteRef Module v0.1.1
//* from Jeong, 2014-04-09
//*
//* Copyright (C) 2014, feelingk.
//* All rights reserved.
//*
//* Use :
//* noteref.init();
//* noteref.bind([<A> element]);
//* noteref.text('text');
//* noteref.show();
//*
//**/
//
//var noteref = new function () {
//    var opt = {
//        content: '[title]',
//        className: 'flk_note', /*클래스 명칭 유니크하게 변경*/
//        width: 'auto',
//        height: 'auto',
//        scroll: 'hidden',
//        position: 'left',
//        extraSpace: 'padding',
//        paddingLeft: 'padding-left',
//        paddingRight: 'padding-right',
////        borderWidth: 'border-width',
//        styleTop: 10,
//        styleWidth: 2,
//        event: 'click,mouseleave,mousemove',
//        root: '#feelingk_bookcontent'
//    };
//
//    var $tip = null;
//    var $inner = null;
////    var $close = null;
//    var $element = {};
//    var visible = false;
//    var saveOption = null;
//
//    var webviewWidth;
//    var webviewHeight;
//    var twoPageViewMode;
//    var isNightMode;
//    var scrollModeBodyMargin;
//    var isPrevent=false;
//    var $fade = null;
//
//    var clone = function (obj, deep) {
//        var objectClone = new obj.constructor();
//        for (var property in obj)
//            if (!deep)
//                objectClone[property] = obj[property];
//            else if (typeof obj[property] == 'object')
//                objectClone[property] = obj[property].clone(deep);
//            else
//                objectClone[property] = obj[property];
//        return objectClone;
//    };
//
//    this.init = function (option) {
//        if (option != null) {
//            opt = option;
//            saveOption = clone(option, false);
//        }
//
//        if($('body').find('*').hasClass('flk_note')){
//        	$inner = $tip.find('div.flk_inner');
//            $fade = $tip.find('div.flk_fade');
//        } else{
//        	$tip = $(['<div id="flk_note" class="', opt.className, '">',
//    			'<div class="flk_inner"></div>',
////    			'<fade class="flk_fade"></fade>',
//    			'<div class="flk_fade"></div>',
//    			'</div>'].join('')).appendTo(document.body);
//            $inner = $tip.find('div.flk_inner');
//            $fade = $tip.find('div.flk_fade');
//            $tip.css('visibility', 'hidden');
//        }
//    };
//
//    this.bind = function (elm) {
//        $element = elm;
//
//        if (opt.event != null && opt.event.trim() != '') {
//            var arr = opt.event.trim().toLowerCase().split(',');
//            for (var i = 0; i < arr.length; i++) {
//            	switch (arr[i]) {
//            	case 'click':
//            		break;
//            	case 'mouseleave':
//            		break;
//            	case 'mousemove':
//            		break;
//            	}
//            }
//        }
//    };
//
//    this.position = function () {
//    };
//
//    this.height = function (value) {
//        opt.height = value;
//    };
//
//    this.width = function (value) {
//        opt.width = value;
//    };
//
//    this.mouseclick = function (e) {
////        $tip.css('visibility', 'hidden');
//    };
//
//    this.mouseleave = function (e) {
////        $tip.css('visibility', 'hidden');
//    };
//
//    this.mousemove = function (e) {
//        this.eventX = e.pageX;
//        this.eventY = e.pageY;
//        return false;
//    };
//
//    this.text = function (content) {
//        $inner.empty().append(content);
//        opt.content = content;
//    };
//
//    this.show = function () {
//        this.reset();
//
//        if(isPrevent){
//        	return;
//        }
//
//        var $win = $(window);
//        var win = {
//            l: $win.scrollLeft(),
//            t: $win.scrollTop(),
//            w: $win.width(),
//            h: $win.height()
//        };
//        var elmOffset = $element.offset();
//        var elm = {
//            l: elmOffset.left,
//            t: elmOffset.top,
//            w: $element.outerWidth(),
//            h: $element.outerHeight()
//        };
//
//        $tip.css('border','20px solid transparent');
//        if(isNightMode){
////         	$('.flk_note').css('background-image', 'url(file:///android_asset/comment_bg_night.png)');
//        	$('.flk_note').css('color', '#ffffff', 'important');
//        	$tip.css('-webkit-border-image','url(file:///android_asset/comment_bg_night.png) 30');
//        	$inner.css('background-color','#3a3a3a');
//        	$inner.find('*').css('background-color','#3a3a3a', 'important');
//        	$fade.css('background-image','url(file:///android_asset/comment_txt_bg_night.png)');
//
//        } else {
////         	$('.flk_note').css('background-image', 'url(file:///android_asset/comment_bg.png)');
//        	$('.flk_note').css('color', '#1a1a1a', 'important');
//        	$tip.css('-webkit-border-image','url(file:///android_asset/comment_bg.png) 30');
//        	$inner.css('background-color','#F7F4DD');
//        	$inner.find('*').css('background-color','#F7F4DD', 'important');
//        	$fade.css('background-image','url(file:///android_asset/comment_txt_bg.png)');
//        }
//
//        var innerPaddingLeftRight = 10;
//        var innerPaddingTopBottom = 10;
//
////        var outerPaddingTopBottom = 28;
//        var outerPaddingLeftRight = 10;
//
//        var outerPaddingTop = 28;
//        var outerPaddingBottom = 42;
//
//        if (opt.width == 'auto') {
//        	opt.width = window.innerWidth - outerPaddingLeftRight*2 - 40;
//        }
//
//        opt.height = Math.ceil(win.h / 3) - 40;
//
//        $inner.css('margin-left', innerPaddingLeftRight, 'important');
//        $inner.css('margin-right', innerPaddingLeftRight, 'important');
//        $inner.css('margin-top', innerPaddingTopBottom, 'important');
//        $inner.css('margin-bottom', innerPaddingTopBottom, 'important');
//
//        $inner.css('width', opt.width-innerPaddingLeftRight*2, 'important');
//        $inner.css('height', opt.height-innerPaddingTopBottom*2, 'important');
//
//        $tip.css('height', opt.height, 'important');
//        $tip.css('width', opt.width,'important');
//
//        $inner.css('overflow-y', 'auto');
//        $inner.scrollTop(0);
//
//        var top;
//        var left;
//
//        if (opt.extraSpace == 'padding')
//            left = win.l + outerPaddingLeftRight;
//        else
//            left = win.l;
//
//        if(animationType!=3){
//
//        	if (elm.t >= win.h / 2){
//        		top = 0 + outerPaddingTop;
//        	}else{
//        		top = win.h - (opt.height + 20) - outerPaddingBottom;
//        	}
//
//        	if(twoPageViewMode == 1 ){
//
//        		if(elm.l >= win.l && elm.l <= win.l+win.w/2){
//        			left = win.l;
//        		} else if(elm.l >= win.l+win.w/2){
//        			left = win.l+win.w/2;
//        		}
//
//        		$tip.css('width', parseInt($tip.css('width'))/2-outerPaddingLeftRight);
//        		$inner.css('width', parseInt($inner.css('width'))/2-innerPaddingLeftRight*2);
//
//        		opt.height = Math.ceil(win.h / 2) - 40;
//        		$inner.css('height', opt.height -innerPaddingTopBottom*2, 'important');
//        		$tip.css('height', opt.height, 'important');
//
//        		if (elm.t >= win.h / 2){
//            		top = 0 + outerPaddingTop;
//            	}else{
//            		top = win.h - (opt.height + 20) - outerPaddingBottom;
//            	}
//        	}
//        } else {
//        	if (elm.t >= (win.t + win.h / 2))
//        		top = win.t + outerPaddingTop -scrollModeBodyMargin;
//        	else
//        		top = win.t + win.h - opt.height - 20 - outerPaddingBottom - scrollModeBodyMargin;
//        }
//
//        if (opt.position == 'target') {
//            $tip.css({ left: elm.l, top: elm.t });
//        } else {
//            $tip.css({ left: left, top: top });
//        }
//
//        $fade.css('width',$inner.width());
//        $fade.css('height',innerPaddingTopBottom);
//        $fade.css({ left: innerPaddingLeftRight, top: $inner.height()});
//        $fade.css('position', 'absolute');
//        $fade.css('background-color','');
//
//        var allImage = $inner.find('img');
//        for(var i=0; i<allImage.length; i++){
//        	var img = allImage[i];
//        	img.style.maxWidth = ($inner).width()+"px";
//        }
//
//        $(".flk_inner").find("p").css("margin-top", "0px");
//        $(".flk_note").css("background-size", parseInt($tip.css('width'))+"px " + parseInt($tip.css('height'))+"px");
//
//        $tip.css('visibility', 'inherit');
//        visible = true;
//    };
//
//    this.hide = function () {
//        $tip.css('visibility', 'hidden');
//        visible = false;
//        var inner = document.getElementsByClassName('flk_inner')[0];
//        while ( inner.hasChildNodes() ) {
//            inner.removeChild( inner.firstChild );
//        }
//    };
//
//    this.status = function () {
//        return visible;
//    }
//
//    this.reset = function () {
//        opt = clone(saveOption, false);
//    }
//
//    this.setCurrentState = function (type, twopage, isNight, bodyMargin){
//    	animationType = type;
//    	twoPageViewMode = twopage;
//    	isNightMode = isNight;
//    	scrollModeBodyMargin=bodyMargin;
//    };
//
//    this.getCurrentMode = function(){
//    	return animationType;
//    }
//
//    this.setPrevent = function(prevent){
//    	isPrevent = prevent;
//    }
//
//    this.isChanged = false;
//    this.animationType;
//} ();
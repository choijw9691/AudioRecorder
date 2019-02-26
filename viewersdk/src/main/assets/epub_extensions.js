/**
 * Epub3.0 epub extensions jQuery Module v0.1.1
 * from Jeong, 2014-03-27
 *
 * Copyright (C) 2014, feelingk.
 * All rights reserved.
 *
 * Use :
 * var epubtype = $.epubtype();
 * var epubswitch = $.epubswitch();
 * var epubtrigger = $.epubtrigger();
 * epubtype.init();
 * epubswitch.init();
 * epubtrigger.init();
 * 
 **/

(function ($) {
	
    var isTouch = 'ontouchstart' in window;
    var events = (isTouch) ? { start: 'touchstart', move: 'touchmove', end: 'touchend' }
			: { start: 'mousedown', move: 'mousemove', end: 'mouseup' };

    $.epubType = function () {
        this.noterefDataList = null;
    };

    $.epubType.prototype = {
        init: function () {
            this.noterefDataList = this.process();

            var opt = {
                content: '[title]',
                className: 'flk_note',
                width: 'auto',
                height: 'auto',
                scroll: 'hidden',
                position: 'left',
                extraSpace: 'padding',
                paddingLeft: 'padding-left',
                paddingRight: 'padding-right',
                borderWidth: 'border-width',
                styleTop: 2,
                styleWidth: 2,
                event: 'click',
                root: '#feelingk_bookcontent'
            };
            
            noteref.init(opt);
            
            for (var i = 0; i < this.noterefDataList.length; i++) {
                var aTag = $(this.noterefDataList[i].element);
               if (aTag.attr("epub\:type") == "noteref") {

                    var ref = $(aTag.attr("href"));
                    var text = ref.html().trim();

                    aTag.click(function (event) {
                        event.preventDefault();
                        
                        var a = $(this);
                        var ref = $(a.attr("href"));
                        var text = ref.html().trim();

                        if(window.selection.getTextSelectionMode()){
                            return;
                        }

                        noteref.bind(a);
                        noteref.position();
                        noteref.text(text);
                        
                        if(noteref.status()){
                        	noteref.hide();
                        	window.selection.setAsidePopupStatus(false);
                        	noteref.isChanged=true;
                        	
                        	if(noteref.getCurrentMode()==3){
                        		var bodyEl = $('body')[0];
                        		bodyEl.style.position = "static";	
                        		bodyEl.style.top = "0px";
                        		window.scrollTo(0,noteref.scrollTop);
                        	}
                            
                        } else{
                        	noteref.show();
                        	window.selection.setAsidePopupStatus(true);
                        	noteref.isChanged=true;
                        	
                        	if(noteref.getCurrentMode()==3){
                        		var scrollTop =  $('body').scrollTop();
                            	noteref.scrollTop = scrollTop;
                        		var bodyEl = $('body')[0];
                        		bodyEl.style.position = "fixed";	
                        		bodyEl.style.top = -1*scrollTop-18 + "px";
                        	}
                        }
                    });
                }
            }

        },
        data: function () {
            return this.noterefDataList;
        },
        process: function () {
            this.noterefDataList = new Array();

            if (this.noterefDataList.length != 0)
                this.noterefDataList = [];

            var noterefArray = $("a").filter(function () {
                return $(this).attr('epub\:type') == 'noteref';
            });

            for (var i = 0; i < noterefArray.length; ++i) {
                var noteref = $(noterefArray[i]);
                var noteLink = $(noteref.attr('href'));
                $(noteLink).hide();

                var noteLinkData = {
                    id: noteLink.attr('id'),
                    epubType: noteLink.attr('epub:type'),
                    innerHtml: noteLink.html().trim(),
                    tagName: $(noteLink)[0].tagName
                }

                var noterefData = {
                    href: noteref.attr('href'),
                    value: noteref.text(),
                    element: noteref,
                    noteLinkData: noteLinkData
                }

                this.noterefDataList[i] = noterefData;
            }
            return this.noterefDataList;
        }
    };

    $.epubSwitch = function () {
        this.NAMESPACE_SVG = "http://www.w3.org/2000/svg";
        this.NAMESPACE_MATHML = "http://www.w3.org/1998/Math/MathML";
    };

    $.epubSwitch.prototype = {
        init: function () {
            var epubSwitchArray = $('epub\\:switch');

            for (var i = 0; i < epubSwitchArray.length; ++i) {
                var children = epubSwitchArray[i].children;
                var hidden = false;

                for (var j = 0; j < children.length; ++j) {
                    if (hidden == true) {
                        $(children[j]).hide();
                    }
                    else if ('epub\:case' == children[j].tagName.toLowerCase()) {
                        var namespace = $(children[j]).attr('required-namespace');
                        if (this.NAMESPACE_SVG == namespace) {
                            hidden = true;
                        }
                        // 네임스페이스 추가시 이곳에 작성
                        //else if (this.NAMESPACE_MATHML == namespace) {
                        //    hidden = true;
                        //}
                        else {
                            $(children[j]).hide();
                        }
                    }
                }
            }
        }
    };

    $.epubTrigger = function () {

    };

    $.epubTrigger.prototype = {
        init: function () {
            var triggerArray = $('epub\\:trigger');
            for (var i = 0; i < triggerArray.length; i++) {
                var trigger = $(triggerArray[i]);
                var triggerElement = document.getElementById(trigger.attr('ev:observer'));
                triggerElement.addEventListener(trigger.attr('ev:event'),
                                                this.triggerAction,
                                                false);
            }
        },
        triggerAction: function (event) {
            var targetId = event.target.id;
            var triggerArray = $('epub\\:trigger');

            for (var i = 0; i < triggerArray.length; i++) {
                var trigger = $(triggerArray[i]);
                if (trigger.attr('ev:observer') == targetId) {
                    var actionElement = document.getElementById(trigger.attr('ref'));
                    var action = trigger.attr('action');
                    //action 에 따른 기능 수행
                    if( actionElement.tagName != 'VIDEO' ){
	                    if (action == "play")
	                        actionElement.play();
	                    else if (action == "pause")
	                        actionElement.pause();
	                    else if (action == "resume")
	                        actionElement.play();
	                    else if (action == "mute")
	                        actionElement.muted = true;
	                    else if (action == "unmute")
	                        actionElement.muted = false;
	                    else if (action == "show")
	                        $(actionElement).css("visibility", "visible");
	                    else if (action == "hide")
	                        $(actionElement).css("visibility", "hidden");
                    } else {
                    	window.selection.controlVideo(actionElement.id, action);
                    }
                }
            }
        }
    };

    $.extend({
        epubtype: function () {
            var epubType = new $.epubType();
            return epubType;
        },
        epubswitch: function () {
            var epubSwitch = new $.epubSwitch();
            return epubSwitch;
        },
        epubtrigger: function () {
            var epubTrigger = new $.epubTrigger();
            return epubTrigger;
        }
    });
})(jQuery);
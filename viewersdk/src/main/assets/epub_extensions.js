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

    $.epubType = function () {
        this.noterefDataList = null;
    };

    $.epubType.prototype = {
        init: function (currentContentType) {
            this.noterefDataList = this.process();

            for (var i = 0; i < this.noterefDataList.length; i++) {
                var noterefData = this.noterefDataList[i];
                var aTag = $(this.noterefDataList[i].element);
                if (aTag.attr("epub\:type") == "noteref") {
                    aTag.on('click', { value: noterefData }, function(event){
                        event.preventDefault();
                        if(currentContentType == "reflowable"){
                             window.selection.handleNoterefData(JSON.stringify(event.data.value));
                        } else if(currentContentType == "fixedlayout"){
                            window.fixedlayout.handleNoterefData(JSON.stringify(event.data.value));
                        }
                        return;
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
                var asideContent="";
                if(noteref.attr('href').indexOf('#')==0){
                    var targetId = noteref.attr('href');
                    asideContent = $(targetId)[0].textContent;
                }
                var noterefData = {
                    href: noteref.attr('href'),
                    value: noteref.text(),
                    asideContent : asideContent,
                    element: noteref.getPath(),
                    position : JSON.stringify(noterefArray[i].getClientRects()[0])
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
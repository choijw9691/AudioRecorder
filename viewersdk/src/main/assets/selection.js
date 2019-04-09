const ELEMENT_NODE = 1;
const TEXT_NODE = 3;
const HIGHLIGHT_CLASS='KYBH';
const UNDERLINE_CLASS='KYBUnderline';
const BODY_NIGHTMODE_CLASS="KYBNightMode";
const SEARCH_HIGHLIGHT='KYBSearchHighlight';
const TTS_HIGHLIGHT_CLASS = "FLKTTSHighlight";
const ANNOTATION_LASTSPAN_CLASS='KYBAnnotatedLastSpan';
const MEMO_CLASS='KYBMemo';

var gPosition = 0;
var gCurrentPage = 0;
var gPageCount = 0;
var gClientHeight = -1;
var gWindowInnerHeight = 0;
var gWindowInnerWidth = 0;
var gRealHeight = 0;
var gMarginLeft = 0;
var gMarginRight = 0;
var gMarginTop = 0;
var gMarginBottom = 0;
var gColumnWidth=0;
var gDeviceType;
var gDirectionType = 0; // 0: ltr , 1 : rtl
var gCurrentViewMode;
var gTwoPageViewMode;
var gOsVersion;
var gNoteRefArray = new Array;
var audioTimerID;
var fontfaceLoadingDone=true;
var chromeAgent;
var currentChromeVersion;
var currentAndroidVersion;
var isPageMoveRequest = false;
var imageDataInfo= new Array();
/********************************************************************************************* s:ready */
$(document).ready(function(){

	var htmlDir = $('html').attr('dir');
    var bodyDir = $('body').attr('dir');

    if( (htmlDir != undefined && htmlDir.toLowerCase() == "rtl") || (bodyDir != undefined && bodyDir.toLowerCase() == "rtl") ){
    	gDirectionType = 1;
    	$('*').css('dir', 'rtl');
    }

    window.selection.reportDirectionType(gDirectionType);

    currentAndroidVersion = getAndroidOsVersion();
    chromeAgent = ((navigator.userAgent || "").match(/chrome\/[\d]+/gi) || [""])[0];
    currentChromeVersion = parseInt((chromeAgent .match(/[\d]+/g) || [""])[0], 10);
    if(35 < currentChromeVersion){
        $('body').css('width', window.innerWidth);
        $('#feelingk_booktable').css('width', window.innerWidth);

        document.fonts.onloadingdone = function (fontFaceSetEvent) {
            fontfaceLoadingDone = true;
        };

        document.fonts.onloading = function (fontFaceSetEvent){
            fontfaceLoadingDone = false;
        }
    }

//  [ssin-audio]autoplay front 제어이므로 일단 전체 끔
    var audios = $('audio');
    for(var i = 0; i < audios.length; i++) {
    	var audioElement = audios[i];
    	if (audioElement.autoplay == true){
    		audioElement.pause();
    	}
    }

    if(currentAndroidVersion == "4.4.2"){
		audios.removeAttr("loop")
	}

    $('*').contents().each(function() {
        if(this.nodeType === Node.COMMENT_NODE) {
            $(this).remove();
        }
    });

    var epubtype = $.epubtype();
    var epubswitch = $.epubswitch();
    var epubtrigger = $.epubtrigger();
    epubtype.init();
    epubswitch.init();
    epubtrigger.init();
    gNoteRefArray = epubtype.data();

	document.body.addEventListener('touchstart', function(event) {

//        if(window.selection.getTextSelectionMode()){
//            event.preventDefault();
//        }
		var touch = event.changedTouches[0];

		if( noteref.isChanged )
			 noteref.isChanged  = false;

    	if( touch.target.tagName.toUpperCase() == 'A' && touch.target.getAttribute('epub\:type') != 'noteref' /*|| touch.target.tagName == 'VIDEO'*/){
//    		event.preventDefault();
    	}
	}, {passive:false});

	document.body.addEventListener('touchend', function(event) {

		var touch = event.changedTouches[0];
		log("touchend touch.target.tagName : " + touch.target.tagName);

    	if( touch.target.tagName.toUpperCase() == 'A' && touch.target.getAttribute('epub\:type') != 'noteref'){
    		event.preventDefault();	//20161228 주석 delete
    	}
	}, false);

	var timer;
	var isScrolling=false;
	var oldScrollY;
	var currentScrollY;

	$('#feelingk_bookcontent').on('scroll touchmove',function(e){
		if(isPreventMediaControl || textSelectionMode || gCurrentViewMode!=3){
			e.preventDefault();
            e.stopPropagation();
            return false;
		} else{
			if(gCurrentViewMode==3){
				if(!isScrolling){
					isScrolling = true;
					oldScrollY = $(document).scrollTop();
					clearTimeout(timer);
					timer = setTimeout( refresh , 200);
				}
			}
		}
	});

	var refresh = function () {

		currentScrollY = $(document).scrollTop();
		if(oldScrollY!==currentScrollY){
			oldScrollY=currentScrollY;
			clearTimeout(timer);
			timer = setTimeout( refresh , 200);
		} else{
			isScrolling = false;
			clearTimeout(timer);
			if(gCurrentViewMode==3){
				window.selection.stopScrolling(window.scrollY/document.body.scrollHeight*100);
				if(isSearchHighlightInCurrentPage())
					removeSearchHighlight();
			}
		}
    };

	$('video').on('webkitfullscreenchange mozfullscreenchange fullscreenchange', function(e) {
        var state = document.fullScreen || document.mozFullScreen || document.webkitIsFullScreen;
        if(state && currentAndroidVersion.charAt(0)>=5){
        	$(this)[0].pause();
			document.webkitCancelFullScreen();
			playVideoFullScreen($(this)[0]);
		}
    });

    $('video').on('play', function(){
    	if(isPreventMediaControl){
    		$(this)[0].pause();
    		window.selection.didPlayPreventMedia($($(this)[0]).getPath(), "video");
    		return false;
    	} else {
    		if(currentAndroidVersion.charAt(0)<=4 && currentAndroidVersion.charAt(2)<=3){
    			playVideoFullScreen($(this)[0]);
    		}
    	}
    	window.selection.stopMediaOverlay();
    });

    $('audio').on('play', function(){
    	if(isPreventMediaControl){
    		$(this)[0].pause();
    		window.selection.didPlayPreventMedia($($(this)[0]).getPath(), "audio");	// video audio 여부랑 path값 전달
    		return false;
    	} else {
    		if (audioTimerID)
            	clearInterval(audioTimerID);

            audioTimerID = setInterval(audioTimer, 1000, $(this)[0]);
            window.selection.didPlayAudio($($(this)[0]).getPath(), this.currentTime);
    	}
    	window.selection.stopMediaOverlay();
	});

    $('audio').on('pause', function(event) {

    	var audioes = $('audio');
    	var isPlaying = false;
    	for (var i = 0; i < audioes.length; ++i) {
    		if (audioes[i].paused == false) {
    			isPlaying = true;
    			break;
    		}
    	}

    	if (isPlaying == false)
    		clearInterval(audioTimerID);

    	if(!isPreventMediaControl){
    		window.selection.didPauseAudio( $($(this)[0]).getPath(), this.currentTime);
    	}
    });

    $('audio').on('ended', function(event) {
        clearInterval(audioTimerID);
        window.selection.didFinishAudio($($(this)[0]).getPath());
    });


    $('img').each(function (i, image) {
        var result = new Object();
        result.src = $(image).attr('src');
        result.display = window.getComputedStyle(image).display;
        imageDataInfo.push(result);
        image.style.display = 'block';
    });
});
/********************************************************************************************* e:ready */

/******************************************************************************************** s:jQuery */
jQuery.fn.getPath=function() {

    if(this.length!=1)
        throw'Requires one element.';

    var path = "";
    var node=this;

    while(node.length) {
        var realNode=node[0];
        var name=realNode.localName;

        if(!name) break;

        name=name.toLowerCase();
        if(realNode.id) {
            return name+'#'+realNode.id.replace(/\./g,'\\.')+(path?'>'+path:'');
        }

        var parent=node.parent();

        var siblings=parent.children(name.replace(':','\\:'));

        if(siblings.length>1) {
            name+=':eq('+siblings.index(realNode)+')';
        }

    	path=name+(path?'>'+path:'');
        node=parent;
    }
    return path;
};

jQuery.fn.searchText = function(Ids,pat,maxLen,twoPageView,currFile) {

    function innerSearch(node, pat) {
        var skip = 0;

        if (node.nodeType == 3) {

    		var pos = node.data.toUpperCase().indexOf(pat);

        	while( pos >= 0 ) {

        		textSnippet = getTextFromSearchWord(node, pat, pos, maxLen);

        		var startElement = getAncestorElementForTextNode(node);
                var startElementPath = $(startElement).getPath();

                var l = $(startElementPath).offset().left;
                var t = $(startElementPath).offset().top;

                var respage = Math.floor((l / window.innerWidth));

                var arrPages = getChapterID(Ids, twoPageView);
                var scrLeft = respage * window.innerWidth;
                var scrRight = (respage+1) * window.innerWidth;
                var lastId = '';

                for(var i=0; i<arrPages.length; i++) {
                	var po = arrPages[i];
                	lastId = po.id;

                	if( (l >= scrLeft && l < scrRight ) &&
            			node.offsetTop >= po.startElement.offsetTop &&
            			node.offsetTop < po.endElement.offsetTop )
                	{
                		break;
                	}
                }

                var result = new Object();
                result.elementPath = startElementPath;
                result.text = textSnippet;
                result.page = respage;
                result.id = lastId;
                result.charOffset = pos;
                result.file = currFile;
                result.keyword = pat;

              	window.paging.reportSearchResult(JSON.stringify(result));

                pos = node.data.toUpperCase().indexOf(pat, pos+1);
        	}

        	skip = 1;
        } else if (node.nodeType == 1 && node.childNodes && !/(script|style)/i.test(node.tagName)) {
            for (var i = 0; i < node.childNodes.length; ++i) {
                i += innerSearch(node.childNodes[i], pat.toUpperCase());
            }
        }

        return skip;
    }

    return this.each(function() {
        innerSearch( this, pat.toUpperCase() );
    });
};

jQuery.fn.removeHighlight = function() {
    return this.find("span." + SEARCH_HIGHLIGHT).each(function() {
        this.parentNode.firstChild.nodeName;
        with (this.parentNode) {
            replaceChild(this.firstChild, this);
            normalize();
        }
    }).end();
};
/******************************************************************************************** e:jQuery */

Element.prototype.indexOf=function(elm) {
    var nodeList=this.childNodes;
    var array=[].slice.call(nodeList,0);
    return array.indexOf(elm);
};

HTMLDocument.prototype.indexOf=function(elm) {
    var nodeList=this.childNodes;
    var array=[].slice.call(nodeList,0);
    return array.indexOf(elm);
};

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
  var rest = this.slice((to || from) + 1 || this.length);
  this.length = from < 0 ? this.length + from : from;
  return this.push.apply(this, rest);
};

/******************************************************************************************** s:after load */
function setupChapter(	highlights,
						deviceType,
						twoPageViewMode,
						isNightMode,
						webviewWidth,
						webviewHeight,
						marginLeft,
						marginTop,
						marginRight,
						marginBottom,
						density,
						osVersion,
						currentViewMode,
						backgroundColor,
						indent,
						pMargin,
						lineHeight,
						fontName,
						maxSelectionLength,
						bodyMargin) {
    try {

    	gDeviceType = deviceType;
        gOsVersion = osVersion;
        gCurrentViewMode = currentViewMode;
        gTwoPageViewMode = twoPageViewMode;
        gMarginLeft = marginLeft;
        gMarginRight = marginRight;
        gMarginTop = marginTop;
        gMarginBottom = marginBottom;
        gMaxSelectionLength = maxSelectionLength;

        noteref.setCurrentState(currentViewMode, twoPageViewMode, isNightMode, bodyMargin);

        if(gMarginTop == null){
        	gMarginTop = 0;
        }

        if(gMarginBottom == null){
        	gMarginBottom = 0;
        }

        if(gMarginLeft == null){
        	gMarginLeft = 0;
        }

        if(gMarginRight == null){
        	gMarginRight = 0;
        }

        if (gClientHeight == -1) {
            gClientHeight = document.getElementById('feelingk_bookcontent').clientHeight;
        }

        if(fontName!="null" && fontName!=""){
        	$('body').find('*').css('font-family', "'" +fontName+"'");
        	$('body').css('font-family', "'" +fontName+"'");
        }

        var first = $($('#feelingk_bookcontent').children()[0]);
        var firstMarginTop = first.css('marginTop');
        first.css('marginTop', '0em', '!important');
        first.css('paddingTop', firstMarginTop, '!important');

        resizingImage();

        applyHighlights(highlights);
        setNightMode(isNightMode,true, backgroundColor);

    } catch(err) {
        log('setupChapter: ' + err);
        window.selection.reportError(0);
    }
}

function resizingImage() {

    try{
        var booktableHeight = $('#feelingk_booktable').height();
        	var bookcontentWidth = $('#feelingk_bookcontent').width();
        	var list = new Array();

        	$('img').each(function (i, image) {
        		var totalMargin = getComputedMargin(image);
        		if ($(image).height() + totalMargin.horizontalMargin > booktableHeight || $(image).width() + totalMargin.verticalMargin > bookcontentWidth) {
        			if ((($(image).height() + totalMargin.horizontalMargin) / booktableHeight) > (($(image).width() + totalMargin.verticalMargin)/bookcontentWidth)) {
        				var obj = new Object();
        				obj.e = image;
        				obj.index = i;
        				obj.width = 'auto';
        				obj.height = (booktableHeight - totalMargin.horizontalMargin) + 'px';
        				list.push(obj);
        			} else {
        				var obj = new Object();
        				obj.e = image;
        				obj.index = i;
        				obj.width = (bookcontentWidth - totalMargin.verticalMargin) + 'px';
        				obj.height = 'auto';
        				list.push(obj);
        			}
        		}
        	});

        	var i=0;
        	$(list).each(function () {
        		this.e.style.width = "";
        		this.e.style.width = this.width;
        		this.e.style.height = "";
        		this.e.style.height = this.height;
        		++i;
        	});

            $('img').each(function(i,image){
                image.style.display = imageDataInfo[i].display;
            });

        	if(i == list.length )
        		window.selection.imgResizingDone();
    } catch(error){
        console.log("resizingImage error : "+error);
    }
}

function getComputedMargin(el) {

    var verticalMargin = 0;
    var horizontalMargin = 0;
    var tmpEl = el;

    while (tmpEl.id != 'feelingk_bookcontent') {      // TODO :: feelingk_booktable
    	var size = window.getComputedStyle(tmpEl);
    	if (size != null) {
    		verticalMargin += Math.max(0, (parseInt(size['margin-left'], 10) || 0))
    		+ Math.max(0, (parseInt(size['margin-right'], 10) || 0))
    		+ (parseInt(size['text-indent'], 10) || 0);

    		var lineHeight = parseInt(size['lineHeight'], 10);

    		if (size['lineHeight'] == "normal") {
    			lineHeight = Math.ceil(parseInt(size['fontSize'], 10) * 1.2) || 0;
    		}

    		horizontalMargin += lineHeight
    		+ Math.max(0, (parseInt(size['margin-top'], 10) || 0))
    		+ Math.max(0, (parseInt(size['margin-bottom'], 10) || 0));

    		if (el != tmpEl) {
    			verticalMargin += Math.max(0, (parseInt(size['padding-left'], 10) || 0))
    			+ Math.max(0, (parseInt(size['padding-right'], 10) || 0));

    			horizontalMargin += Math.max(0, (parseInt(size['padding-top'], 10) || 0))
    			+ Math.max(0, (parseInt(size['padding-bottom'], 10) || 0));
    		}

    		tmpEl = tmpEl.parentElement;
    	}
    }

    var totalMargin = new Object();
    totalMargin.verticalMargin = verticalMargin;
    totalMargin.horizontalMargin = horizontalMargin;
    return totalMargin;
}

/******************************************************************************************** s: highlight */
function applyHighlights(highlights) {
    try {
        for(var i=0;i<highlights.length;i++) {
            applyHighlight(highlights[i]);
            if(highlights[i].memo.length>0){
            	var highlightSpans=$("[title=\""+highlights[i].highlightID+"\"]");
            	highlightSpans.addClass(MEMO_CLASS);
            }
        }
            setMemoIcon();
    } catch(err) {
        log(err);
    }
}

function applyHighlight(highlight) {
    try {
        var startElement=getNodeFromElementPath(highlight.startElementPath, 0);
        var endElement=getNodeFromElementPath(highlight.endElementPath, 0);

        if(startElement===null) {
            log("No highlight start element");
        } else if(endElement===null) {
            log("No highlight end element");
        }else {
            var range=document.createRange();
            if(setPointInRange(startElement,highlight.startCharOffset,range,setRangeStart)){
                if(setPointInRange(endElement,highlight.endCharOffset,range,setRangeEnd)){
                    highlightRange(range,highlight.isDeleted,highlight.isAnnotation,highlight.highlightID,highlight.colorIndex);
                }else{
                    log("Could not set end of selection range");
                }
            }else{
                log("Could not set start of selection range");
            }
        }
    } catch(err) {
        log("applyHighlight: "+err);
    }
}

function findHighlight(x, y, element) {

    contextMenuTargetPosition="END";

    var highlightID = null;
    var isHighlight = false;

	try {

        if( element!==null && $(element).hasClass(HIGHLIGHT_CLASS) ){
            highlightID=element.title;
            isHighlight = true;
        }

	    while(!isHighlight && element!==null && element.id !== "feelingk_bookcontent"){
	        element=element.parentNode;
            if( element!==null && $(element).hasClass(HIGHLIGHT_CLASS) ){
                highlightID=element.title;
                isHighlight = true;
                break;
            }
	    }

	    if( isHighlight && highlightID !== null ) {
	         showCurrentHighlightSelection(highlightID);
	    }

	    return isHighlight;
	} catch(error) {
	    console.log("findHighlight error : "+error);
	}
}

function saveHighlight(highlight) {
    if(highlightIsValid(highlight)){
        window.selection.saveHighlight(JSON.stringify(highlight));
        return true;
    }
    return false;
}

function highlightIsValid(highlight){
    return highlight!==null;
}

function highlightSpans(spans, deleted, isAnnotation, highlightID, colorIndex) {

    for(var i=0;i<spans.length;i++){
    	var outerSpan = spans[i];
    	outerSpan.title = highlightID;
    	$(outerSpan).addClass(highlightID);
    	$(outerSpan).addClass(HIGHLIGHT_CLASS);
        $(outerSpan).addClass('FLKAnnotationColor'+colorIndex);
//        $(outerSpan).addClass('FLKAnnotationFontColor');	    // 주석 폰트 색상은 무조건 검정으로 -> TODO :: 20190219 정책 변경됨 - 모든 폰트 컬러 원본 유지
    }

    if (spans.length > 0) {
        $(spans[spans.length-1]).addClass(ANNOTATION_LASTSPAN_CLASS);
    }
}

function deleteHighlight(highlight) {
	try {
    	var id = highlight.highlightID;
    	console.log("deleteHighlight id : " + id);
        var highlightSpans=$("[title=\"" + id + "\"]");
        if( highlightSpans != null ) {
        	$(highlightSpans).contents().unwrap();
        }
        var snode = $(highlight.startElementPath)[0];
        var enode = $(highlight.endElementPath)[0];
        snode.normalize();
        enode.normalize();
	} catch(err) {
		console.log('deleteAllHighlights : ' + err);
	}
}

function deleteHighlights( highlights) {
	try {
	    for(var i=0;i<highlights.length;++i) {
	    	var id = highlights[i].highlightID;
	        var highlightSpans=$("[title=\"" + id + "\"]");
	        if( highlightSpans != null ) {
	        	$(highlightSpans).contents().unwrap();
	        }
	        var snode = $(highlights[i].startElementPath)[0];
	        var enode = $(highlights[i].endElementPath)[0];
	        snode.normalize();
	        enode.normalize();
	    }
	    setMemoIcon();
	} catch(err) {
		log('deleteHighlights : ' + err);
	}
}

function deleteAllHighlights(highlights) {
	try {
	    for(var i=0;i<highlights.length;++i) {
	    	var id = highlights[i].highlightID;
	    	log("deleteHighlights " + i + ", id : " + id);
	        var highlightSpans=$("[title=\"" + id + "\"]");
	        if( highlightSpans != null ) {
	        	$(highlightSpans).contents().unwrap();
	        }
	        var snode = $(highlights[i].startElementPath)[0];
	        var enode = $(highlights[i].endElementPath)[0];
	        snode.normalize();
	        enode.normalize();
	    }
	    setMemoIcon();
	} catch(err) {
		log('deleteAllHighlights : ' + err);
	}
}

function getPageCount(twoPageViewMode){

    if(!fontfaceLoadingDone){
        if(document.fonts.size>0){
            document.fonts.onloadingdone = function (fontFaceSetEvent) {
                fontfaceLoadingDone = true;
                getPageCount(twoPageViewMode);
            };
        }
    } else {
        var otherWidth;
        var otherHeight;

        if (gWindowInnerHeight <= 0){
            if (window.devicePixelRatio < 1.0) {
                gWindowInnerHeight = window.outerHeight;
                otherHeight = window.innerHeight;
            } else {
                gWindowInnerHeight = window.innerHeight;
                otherHeight = window.outerHeight;
            }
        }

        if (gWindowInnerWidth <= 0){
            if (window.devicePixelRatio < 1.0) {
                gWindowInnerWidth = window.outerWidth;
                otherWidth = window.innerWidth;
            } else {
                gWindowInnerWidth = window.innerWidth;
                otherWidth = window.outerWidth;
            }
        }

        gRealHeight = gWindowInnerHeight - (gMarginTop + gMarginBottom);
        gColumnWidth = gWindowInnerWidth / getNumColumns(twoPageViewMode) - getColumnGap(twoPageViewMode);
        gPageCount = Math.ceil(document.body.scrollWidth / getWindowWidth(twoPageViewMode));
        if ( gPageCount == 0 ) {
            gPageCount = 1;
        }

        if(twoPageViewMode == 1 && document.body.scrollWidth % getWindowWidth(twoPageViewMode) != 0 ){
            log('setup twoPageViewMode == 1 >> ' + document.body.scrollWidth / getWindowWidth(twoPageViewMode) + ', gCount : ' + gPageCount);
            modifyBookContents();
            gPageCount = Math.ceil(document.body.scrollWidth / getWindowWidth(twoPageViewMode));
        }

        console.log('setup >> ' + document.body.scrollWidth + ', ' + getWindowWidth(twoPageViewMode) + ', '+gPageCount);

        callPageReady(twoPageViewMode);
    }
}

function callPageReady(twoPageMode) {
    var windowWidth = getWindowWidth(twoPageMode);
    window.selection.pageReady(gPageCount, windowWidth);
}

function setNightMode(isNightMode, doCallback, backgroundColor) {

    if(isNightMode==true || isNightMode==1) {
        $(document.body).addClass(BODY_NIGHTMODE_CLASS);
    } else if(isNightMode!=true && isNightMode!=1){
        $(document.body).removeClass(BODY_NIGHTMODE_CLASS);
    }

    var bodyStyle=document.body.style;
    var textColor;

    if(isNightMode==true || isNightMode==1) {
        textColor="#bebebe";
        $('body').find('*').not('a').not($('#flk_note')).not($('#flk_note').find('*')).css('backgroundColor', backgroundColor, 'important');
        $('body').find('*').not('a').not($('#flk_note')).not($('#flk_note').find('*')).css('color', textColor, 'important');
        $('body').find('a').css('color','#6887f7', 'important');
        $('body').css('backgroundColor', backgroundColor, 'important');
        $('body').css('color', textColor, 'important');
    } else if(isNightMode!=true && isNightMode!=1){
    	if(backgroundColor != null && backgroundColor.toUpperCase() != "#FFFFFF")
    		$('body').css('backgroundColor', backgroundColor, 'important');
    }

    if(doCallback) {
        window.selection.turnOnNightModeDone(isNightMode==1 ? true : false);
    }
}

function getColumnGap(twoPageViewMode) {
    if (twoPageViewMode == 1) {
        return 0;
    } else {
        return 0;
    }
}

function getNumColumns(twoPageViewMode){
    if (twoPageViewMode == 1) {
        return 2;
    } else{
        return 1;
    }
}

function getWindowWidth(twoPageViewMode) {
//    if (twoPageViewMode == 1) {
//        var width = gWindowInnerWidth + Math.ceil(getColumnGap(twoPageViewMode)/parseFloat(getNumColumns(twoPageViewMode)));
//
//        if (width % getNumColumns(twoPageViewMode) != 0) {
//            width = width - (getNumColumns(twoPageViewMode)-1);
//        }
//
//        return width;
//    }
//    else {
        return gWindowInnerWidth;
//    }
}

function setupBookColumns(twoPageViewMode) {
    //2페이지 모드에서 페이지 갯수가 홀수일 경우 챕터 끝에서 반페이지만 넘어가는 현상 수정
//    if(twoPageViewMode == 1 && document.body.scrollWidth % getWindowWidth(twoPageViewMode) != 0 ){
//    	log('setup twoPageViewMode == 1 >> ' + document.body.scrollWidth / getWindowWidth(twoPageViewMode) + ', gCount : ' + gPageCount);
//    	modifyBookContents();
//    	gPageCount = Math.ceil(document.body.scrollWidth / getWindowWidth(twoPageViewMode));
//    }
}

function modifyBookContents() {
    var rootElementNode = $('#feelingk_bookcontent')[0];
    var node = getRootChildNodeInChildren(rootElementNode);
    var lastNode = node.children[node.children.length - 1];
    var appendNode = null;
    if (lastNode.tagName.toUpperCase() == "DIV") {
        appendNode = document.createElement('div');
        appendNode.id = "feelingk_modify_bookcontent";
    } else {
        appendNode = document.createElement('p');
        appendNode.id = "feelingk_modify_bookcontent";
    }

    $(node).append(appendNode);
    $('#feelingk_modify_bookcontent').css('width', (gColumnWidth - gMarginLeft - gMarginRight) + 'px'); // -80은 최대 마진값
    $('#feelingk_modify_bookcontent').css('height', (gRealHeight) + 'px');
}
/******************************************************************************************** e:after load */

function generateUniqueClass(){
	var randID='kyoboId'+Math.floor(Math.random()*10001);
	while($("."+randID).length)
		randID='kyoboId'+Math.floor(Math.random()*10001);
	return randID;
}

function getSpanClientRects(span) {

	if( span === null ) {
		return null;
	}

	var rect;
	var rects=new Array();

	if(span.getClientRects) {
		var clientRects=span.getClientRects();
		for(var i=0;i<clientRects.length;++i){
			rects.push( getRectangleObject(clientRects[i].left, clientRects[i].top, clientRects[i].width, clientRects[i].height) );
		}
		return rects;
	}
	rect=getElementRect(span);
	rects.push(rect);
	return rects;
}

function getSpanElementRects(span, jqOffsetLeft) {

	if( span === null ) {
		return null;
	}

	var rect;
	var rects=new Array();

	if(span.getClientRects) {
		var clientRects=span.getClientRects();
		for(var i=0;i<clientRects.length;++i) {
			var left = clientRects[i].left;
			if( gDirectionType == 1 ) //rtl이면
				left = left - gWindowInnerWidth;
			if(jqOffsetLeft != left){
				left = left+document.body.scrollLeft;
			}
			rects.push( getRectangleObject(left, clientRects[i].top+document.body.scrollTop, clientRects[i].width, clientRects[i].height) );
		}
		return rects;
	}
	rect=getElementRect(span);
	rects.push(rect);
	return rects;
}

function getRectangleObject(left,top,width,height){
    var rect={'left':left,'top':top,'width':width,'height':height};
    return rect;
}

function getSelectionRectObject(left,top,right,bottom,width,height){
    var rect={'left':left,'top':top,'right':right,'bottom':bottom,'width':width,'height':height};
    return rect;
}
function getElementRect(element) {
    return getRectangleObject(element.offsetLeft,element.offsetTop,element.offsetWidth,element.offsetHeight);
}

function highlightRange(range, isDeleted, isAnnotation, highlightID, colorIndex) {
    var spans = new Array();
    try {
        surroundRangeWithSpan(range, spans);
        highlightSpans(spans, isDeleted, isAnnotation, highlightID, colorIndex);
    } catch(err) {
        log('highlightRange: ' + err);
    }
    return spans;
}

function surroundRangeWithSpan(range, spans) {
    try{
    	var span=document.createElement('flk');
        range.surroundContents(span);
        cullBlankTextNodesAroundSpan(span);
        spans.push(span);
    } catch(err){
        surroundRangeComponentsWithSpan(range, spans);
    }
    return span;
}

function cullBlankTextNodesAroundSpan(span) {

    var parent=span.parentNode;
    var i=parent.indexOf(span);

    if(i>0) {
        var nodeBefore=parent.childNodes[i-1];
        if(nodeBefore.nodeType==TEXT_NODE&&nodeBefore.nodeValue===''){
            parent.removeChild(nodeBefore);
        }
    }

    if(i+1<parent.childNodes.length) {
        var nodeAfter=parent.childNodes[parent.childNodes.length-1];
        if(nodeAfter.nodeType==TEXT_NODE&&nodeAfter.nodeValue==='') {
            parent.removeChild(nodeAfter);
        }
    }
}

function surroundRangeComponentsWithSpan(range, childSpans) {

    var childCount=0;
    var childSpan=null;

    try {
        var nodesToHighlight=new Array();
        findAllFullTextNodesInRange(nodesToHighlight, range);

        if(range.startContainer.nodeType==TEXT_NODE&&nodesToHighlight.indexOf(range.startContainer)<0){
            var endOffset=range.startContainer.nodeValue.length;childSpan=createSpanForTextNodeContainer(range.startContainer,range.startOffset,endOffset,childCount);
            if(childSpan!==null){
                childSpans.push(childSpan);++childCount;
            }
        }

        for(var i=0;i<nodesToHighlight.length;i++){
            var node=nodesToHighlight[i];
            if(node.nodeValue!==""){
                childSpan=createSpanForTextNodeContainer(node,0,node.nodeValue.length,childCount);
                if(childSpan!==null){
                    childSpans.push(childSpan);++childCount;
                }
            }
        }

        if(range.endContainer.nodeType==TEXT_NODE&&nodesToHighlight.indexOf(range.endContainer)<0){
            childSpan=createSpanForTextNodeContainer(range.endContainer,0,range.endOffset,childCount);
            if(childSpan!==null) {
                childSpans.push(childSpan);++childCount;
            }
        }
    } catch(err) {
        log('components exc: '+err);
    }
}

function findAllFullTextNodesInRange(textNodes, range) {
    try {
        var currentNode=firstAncestralSibling(range.startContainer);
        if(range.startContainer.nodeType==ELEMENT_NODE) {
            if(range.startContainer.childNodes.length>range.startOffset) {
                currentNode=range.startContainer.childNodes[range.startOffset];
            }
        } else if(range.startOffset===0) {
            currentNode=range.startContainer;
        }

        if(currentNode===null){
            return;
        }

        var endNode=firstAncestralSibling(range.endContainer);

        if(range.endContainer.nodeType==ELEMENT_NODE) {
            if(range.endContainer.childNodes.length>range.endOffset) {
                endNode=range.endContainer.childNodes[range.endOffset];
            }
        } else if(range.endOffset<range.endContainer.nodeValue.length) {
            endNode=range.endContainer;
        }

        while(currentNode!==null&&currentNode!=endNode) {
        	if(currentNode.nodeType===TEXT_NODE&&currentNode.textContent!=='' && currentNode.textContent.trim() !== '') {// Jeong, 2013-10-30 : 개행노드나 공백노드 제외
                //&&currentNode.textContent!=='\n'&&currentNode.textContent!=='\r\n')
                textNodes.push(currentNode);
            }
            currentNode=nextNode(currentNode);
        }
    } catch (err) {
        log(err);
    }
}

function setPointInRange(node,charOffset,range,setter){

    if(node===null) {
        return false;
    }

    if(charOffset===0) {
        setter(range,node,0);
        return true;
    }

    while (node != null && node != document) {
        // Jeong, 2013-07-10 : node가 ELEMENT_NODE일 경우와 TEXT_NODE일 경우만 검사
        if (node.nodeType === ELEMENT_NODE || node.nodeType === TEXT_NODE) {
            if (node.textContent.length >= charOffset)
                break;
        }

        var sibling=firstAncestralSibling(node);

        if(sibling===null) {
            if(node.nodeType===ELEMENT_NODE) {
                setter(range,node,node.childNodes.length);
            } else if(node.nodeType===TEXT_NODE) {
                setter(range,node,node.textContent.length);
            }
            return true;
        }

    	// Jeong, 2013-07-10 : <!-- --> 주석달린 부분의 텍스트는 계산하지 않음.
    	if (node.nodeType === TEXT_NODE || node.nodeType === ELEMENT_NODE)
            charOffset-=node.textContent.length;

        node=sibling;
    }

    var children;

    if(node.nodeType===ELEMENT_NODE){
        children=node.childNodes;
    } else if(node.nodeType===TEXT_NODE) {
        children=new Array(node);
    } else {
        return false;
    }

    var cumulativeOffset=0;

    for(var i=0;i<children.length;++i) {
        if(children[i].nodeType===ELEMENT_NODE||children[i].nodeType===TEXT_NODE) {
            var textContent=children[i].textContent;
            var newOffset=cumulativeOffset+textContent.length;
            if(newOffset>=charOffset) {
                if(children[i].nodeType===ELEMENT_NODE) {
                    return setPointInRange(children[i],charOffset-cumulativeOffset,range,setter);
                } else {
                    setter(range,children[i],charOffset-cumulativeOffset);
                    return true;
                }
            } else {
                cumulativeOffset=newOffset;
            }
        }
    }
    return false;
}

function setRangeEnd(range,endContainer,endOffset){
    range.setEnd(endContainer,endOffset);
}

function setRangeStart(range,startContainer,startOffset){
    range.setStart(startContainer,startOffset);
}

function firstAncestralSibling(node) {
    if(node===null||node.parentNode===null){return null;}

    var parent=node.parentNode;

    try {
        var i=parent.indexOf(node);
        if(i+1<parent.childNodes.length) {
            return parent.childNodes[i+1];
        } else {
            return firstAncestralSibling(parent);
        }
    } catch(err) {
        log('firstAncestralSibling: '+err);
    }
    return null;
}

function nextNode(node) {
    if(node===null){return null;}

    var result=null;
    if(node.nodeType==ELEMENT_NODE && node.childNodes.length>0) {
        result=node.childNodes[0];
    } else {
        result=firstAncestralSibling(node);
    }
    return result;
}

function firstSibling(node) {

    if(node===null||node.parentNode===null){return null;}

	if( node.tagName.toUpperCase() =='IMG' || node.tagName.toUpperCase() =='A')
		return node;

	var parent = node.parentNode;

	try {
		var i=parent.indexOf(node);
		if( i+1 < parent.childNodes.length ) {
			return parent.childNodes[i+1];
		} else {
			return firstSibling(parent);
		}
	} catch(err) {
        log("firstSibling err : "+err);
	}
	return null;
}

function getChildIndex(docRoot, path, rootContainer) {

	path = stringDotRevision(path);

	var index=0;
	var node = $(path)[0];

    if(docRoot.length > 1) {
        for(var i=0; i<docRoot.length; i++, index++) {
            var child = docRoot[i];
            if (child.className.indexOf(HIGHLIGHT_CLASS) != -1) {
                index--;
            } else if( child == node ) {
	            return index;
            }
        }
    } else {
        var node = $(path)[0];
        var elementNode = node;

        while(node != null && node != document) {
        	if(node.parentNode == rootContainer) {

              	var childrenNode = getRootChildNodeInChildren(node);

              	if(elementNode == childrenNode){
                  	index = 0;
                  	break;
          		}

              	for(var i=0; i<childrenNode.children.length; i++) {
                  	var currentNode = elementNode;

                  	if(currentNode == childrenNode.children[i])
                      	break;
                  	else
                      	index++;
              	}
              	break;
          	} else {
              	node = node.parentNode;
            }
        }
    }
    return index;
}

function getRootChildNode(elementNode, rootContainer) {

    var node = elementNode;
    var childrenNode;

    while(node != null && node != document) {
        if(node.parentNode == rootContainer) {
            childrenNode = getRootChildNodeInChildren(node);
            if (elementNode == childrenNode){
                return childrenNode;
            } else if (node == childrenNode) {
            	if (elementNode.localName == 'flk' && (elementNode.className.indexOf(HIGHLIGHT_CLASS) != -1))
                    return childrenNode;
                if (childrenNode.children.length > 1 && childrenNode.tagName.toUpperCase() == "DIV") {
                    // 부모의 자식 태그가 2개 이상일 경우 현재경로를 리턴한다.
                    if (childrenNode.parentNode.children.length > 1)
                        return childrenNode;
                    // 부모의 자식 태그가 1개 일 경우 그 하단경로로 이동한다.
                    else
                        childrenNode = childrenNode;
                } else {
                    return childrenNode;
                }
            } else if (node == childrenNode){
                return childrenNode;
            }

            for(var i=0; i<childrenNode.childNodes.length; i++) {
                var currentNode = elementNode;
                while(currentNode!==null&&currentNode!=rootContainer) {
                    if(currentNode == childrenNode.childNodes[i]) {
                        return childrenNode.childNodes[i];
                    }
                    currentNode=currentNode.parentNode;
                }
            }
            return node;
        } else {
            node = node.parentNode;
        }
    }
}

function getRootChildNodeInChildren(elementNode) {
    var node = elementNode;
    while(true) {
        if(node.parentNode.children.length == 1 && node.children.length == 1 && node.tagName.toUpperCase() == "DIV")
            node = node.children[0];
        else
            return node;
    }
}

//컨테이너를 받아서 이전 노드들의 캐릭터 오프셋을 구한다.
function prevNodeCharOffset(elementNode, container) {
    var charOffset = 0;
    var currentNode = elementNode;

    while(currentNode!==null&&currentNode!=container) {
        if(currentNode.nodeType===TEXT_NODE&&currentNode.textContent!=='') {
            var contentLength = currentNode.textContent.length;
            charOffset += contentLength;
        }
        currentNode=nextNode(currentNode);
    }
    return charOffset;
}

//텍스트 노드의 조상 엘리먼트를 가져온다
function getAncestorElementForTextNode(node) {
    var element = node;
    if (element.nodeType != ELEMENT_NODE) {
        element = element.parentNode;
    }
    return element;
}

// path에 .이 있을경우 jquery 사용을 위해 \\. 으로 보정을 한다.
function stringDotRevision(StrPath){
	StrPathOne = StrPath.replace(/\\\./g,'.');
	StrPathTwo = StrPathOne.replace(/\./g,'\\.');
	return StrPathTwo;
}

//Jeong, 2013-11-15 : Highlight 노드를 제외한 부모노드 계산
function getNodeWithoutHighlight(elementNode) {
    var node = elementNode;
    while (node != null && node != document) {
        if (node.localName != 'flk') {
            return node;
        } else if (node.localName == 'flk' && (node.className.indexOf(HIGHLIGHT_CLASS) == -1)) {
            return node;
        } else {
            node = node.parentNode;
        }
    }
}

function createSpanForTextNodeContainer(container,startOffset,endOffset,childCount) {
    var childSpan=null;
    var rng=document.createRange();
    rng.setStart(container,startOffset);
    rng.setEnd(container,endOffset);

    if(rng.toString()!==""){
        childSpan=document.createElement("flk");
        rng.surroundContents(childSpan);
        cullBlankTextNodesAroundSpan(childSpan);
    }
    rng.detach();

    return childSpan;
}

function getHighlightData(highlightID, startPath, endPath, startIndex, endIndex, startOffset, endOffset,clrIndex, memoContent){
    try{
        var highlight=new Object();
        highlight.highlightID=highlightID;
        highlight.startElementPath=startPath;
        highlight.startChildIndex=startIndex;
        highlight.startCharOffset=startOffset;
        highlight.endElementPath=endPath;
        highlight.endChildIndex=endIndex;
        highlight.endCharOffset=endOffset;
        highlight.isDeleted=false;
        highlight.isAnnotation=true;
        highlight.colorIndex=clrIndex;
        highlight.memo=memoContent;
        return highlight;
    } catch(err){
        log('getHighlightData: '+err);
    }
    return null;
}

function getNodeFromElementPath(containerElementPath,containerChildIndex) {

	containerElementPath = stringDotRevision(containerElementPath);

    try {
        var containers=$(containerElementPath);
        if(containers.length<1){
            log('could not find container at path '+ containerElementPath);
            return null;
        }

        var containerNode;
        if(containerChildIndex==-1) {
            containerNode=containers[0];
        } else if(containerChildIndex<containers[0].childNodes.length){
            containerNode=containers[0].childNodes[containerChildIndex];
        } else {
            containerNode=firstAncestralSibling(containers[0]);
        }
        return containerNode;
    } catch(err) {
        log('getNodeFromElementPath: '+err);
    }
    return null;
}

function findTagUnderPoint(x,y,singleTap) {

 	var url=null;
    var tagType=-1;

    try {
        // aside popup 영역 예외처리
    	var element = document.elementFromPoint(x, y);
    	if(element.className == "flk_inner")
    		return;

    	if( noteref.isChanged ){
    		return;
    	} else if( !noteref.isChanged && noteref.status()){
    		var innerExist = false;
    		var parents = $(element).parents();
    		for (var i=0; i<parents.length; i++) {
				var parent = parents[i];
				if (parent.className == "flk_inner") {
					innerExist=true;
					break;
				}
			}

    		if(!innerExist){
    			noteref.hide();
    			window.selection.setAsidePopupStatus(false);
    			if(gCurrentViewMode==3){
        			var bodyEl = $('body')[0];
            		bodyEl.style.position = "static";
            		bodyEl.style.top = "0px";
            		window.scrollTo(0,noteref.scrollTop);
            	}
    			return;
    		}
    	}

     	log("-------------------------------------------------tagName : " + element.tagName);

        //trigger 버튼 예외 처리
     	var targetId = element.id;
        var triggerArray = $('epub\\:trigger');
        for (var i = 0; i < triggerArray.length; i++) {
            var trigger = $(triggerArray[i]);
            if (trigger.attr('ev:observer') == targetId) {
            	log("-------------------------------------------------trigger id :" +targetId);
            	return;
            }
        }

        if( element.tagName.toUpperCase() == 'BUTTON' ) {
     		return;
     	}

        // 재활성화 여부를 위해 flk 검사
        var isExceptionalTagOrAttr = findHighlight(x, y, element);

        if( element.tagName.toUpperCase() == 'BUTTON') {
            isExceptionalTagOrAttr = true;
        }

        if(element.hasAttribute('onclick')){
            isExceptionalTagOrAttr = true;
        }

        var role = element.getAttribute('role');
        if(role!=null && role!=undefined && role.toLowerCase().indexOf("button") != -1){
            isExceptionalTagOrAttr = true;
        }

        if( element.nodeType == ELEMENT_NODE ) {
            if( element.tagName.toUpperCase() =='IMG' || element.tagName.toUpperCase() =='A'|| element.tagName.toUpperCase() =='AUDIO'|| element.tagName.toUpperCase() =='VIDEO') {
                url = element.outerHTML;
                if(element.tagName.toUpperCase() =='IMG' ){
                    tagType = 1;
                    url = element.src;
                    while(element.id != "feelingk_bookcontent"){	// img 태그 부모가 a tag 인 경우를 위해 부모 검사
                        element = element.parentNode;
                        if(element.tagName.toUpperCase()=='A'){
                            tagType = 2;
                            url = element.href;
                            break;
                        }
                    }
                } else if(element.tagName.toUpperCase() =='A'){
                    tagType = 2;
                    url = element.href;
                } else if(element.tagName.toUpperCase() =='AUDIO'){
                    tagType = 3;
                } else if(element.tagName.toUpperCase() =='VIDEO'){
                    tagType = 4;
                    url = element.src;
                }
            } else {
                var parent = element.parentElement;
                if(parent != null){
                    if( parent.tagName.toUpperCase() =='IMG' || parent.tagName.toUpperCase() =='A' || parent.tagName.toUpperCase() =='AUDIO' || parent.tagName.toUpperCase() =='VIDEO') {
                        url = parent.outerHTML;
                        if(parent.tagName.toUpperCase() =='IMG' ){
                            tagType = 1;
                            url = parent.src;
                        } else if(parent.tagName.toUpperCase() =='A'){
                            tagType = 2;
                            url = parent.href;
                        } else if(parent.tagName.toUpperCase() =='AUDIO'){
                            tagType = 3;
                        } else if(parent.tagName.toUpperCase() =='VIDEO'){
                            tagType = 4;
                            url = element.src;
                        }
                    }

                    if(parent.hasAttribute('onclick')){
                        isExceptionalTagOrAttr = true;
                    }

                    var role = parent.getAttribute('role');
                    if(role!=null && role!=undefined && role.toLowerCase().indexOf("button") != -1){
                        isExceptionalTagOrAttr = true;
                    }
                }
            }
        }
    } catch(err) {
 	    console.log('findTagUnderPoint = ' + err);
 	    url=null;
 	}
    window.selection.HitTestResult(url, tagType, x, y, singleTap, isExceptionalTagOrAttr);
}


function getSvgTag() {
    var svgs = $('svg');
    for (var i = 0; i < svgs.length; i++) {
        var svgElement = svgs[i];
        var posLeft = $(svgElement).offset().left - $(document).scrollLeft();
        var posTop = $(svgElement).offset().top - $(document).scrollTop();
        var posRight = posLeft + $(svgElement).width();
        var posBottom = posTop + $(svgElement).height();
        if (posLeft > 0 && posLeft < window.innerWidth) {   //화면에 위치하면
        	svgElement.setCurrentTime(0);
        }
    }
}

function checkContainSVG(range) {
    var selectionHtml = "";
    var container = document.createElement("div");
    container.appendChild(range.cloneContents());
    selectionHtml = container.innerHTML.toLowerCase();
    if (selectionHtml.indexOf('<svg') != -1)
        return true;
    else
        return false;
}

function checkAudioTag(){
	//모든 오디오 찾기
	var audios = $('audio');

	for(var i = 0; i < audios.length; i++) {
		var audioElement = audios[i];
		//오디오 정지
//		audioElement.pause();

		if (audioElement.autoplay == true){
			var posLeft = $(audioElement).offset().left - $(document).scrollLeft();
			var posTop = $(audioElement).offset().top - $(document).scrollTop();
			var posRight = posLeft + $(audioElement).width();
			var posBottom = posTop + $(audioElement).height();

			//현재 페이지에 존재하는 오디오만 처리
			if(posLeft > 0 && posRight < gWindowInnerWidth) {

				//결과값에 SRC 추가
				var valueSRC = "";
				if(audioElement.src.length > 0) {
					valueSRC = audioElement.src;
				} else {
					for(var j = 0; j < audioElement.children.length; j++) {
						if(audioElement.children[j].src.length > 0) {
							valueSRC = audioElement.children[j].src;
							break;
						}
					}
				}
			}
		}
	}
}

function getVideoTag(){

	var videos = $('video');

	var videoArray = new Array();

	for(var i = 0; i < videos.length; i++) {
		var videoElement = videos[i];

		//비디오 정지
		videoElement.pause();

		var scrollTop = $(document).scrollTop();
		var scrollBottom = $(document).scrollTop() + gWindowInnerHeight;
		var posTop = $(videoElement).offset().top - $(document).scrollTop();
		var posBottom = posTop + $(videoElement).height();
		var posLeft = $(videoElement).offset().left - $(document).scrollLeft();
		var posRight = posLeft + $(videoElement).width();

		if(gCurrentViewMode==3) {
			if(posTop > scrollTop && posBottom < scrollBottom){
				//결과값에 SRC 추가
				var valueSRC = "";
				if(videoElement.src.length > 0) {
					valueSRC = videoElement.src;
				} else {
					for(var j = 0; j < videoElement.children.length; j++) {
						if(videoElement.children[j].src.length > 0) {
							valueSRC = videoElement.children[j].src;
							if( videoElement.children[j].src.indexOf(".mp4") > -1 ){
								break;
							}
						}
					}
				}

				if(valueSRC.length > 0) {
					if(videoElement.autoplay == true){
						videoElement.play();
						break;
					}
				}
			}
		} else {
			if(posLeft > 0 && posRight < gWindowInnerWidth){
				//결과값에 SRC 추가
				var valueSRC = "";
				if(videoElement.src.length > 0) {
					valueSRC = videoElement.src;
				} else {
					for(var j = 0; j < videoElement.children.length; j++) {
						if(videoElement.children[j].src.length > 0) {
							valueSRC = videoElement.children[j].src;
							if( videoElement.children[j].src.indexOf(".mp4") > -1 ){
								break;
							}
						}
					}
				}

				if(valueSRC.length > 0) {
					if(videoElement.autoplay == true){
						videoElement.play();
						break;
					}
				}
			}
		}
	}
}

function stopAllMedia(){
	stopAllVideo();
	stopAllAudio();
}

function stopAllVideo(){
	var videos = $('video');
	for(var i = 0; i < videos.length; i++) {
		var videoElement = videos[i];
		videoElement.pause();
	}
}

function stopAllAudio(){
	var audios = $('audio');
	for(var i = 0; i < audios.length; i++){
		var audioElement = audios[i];
		audioElement.pause();
	}
}

function getElementsByClassName(cls) {

    var retnode = [];
    var myclass = new RegExp(cls, 'g');
    var elem = document.getElementsByTagName('*');

    for (var i = 0; i < elem.length; i++) {
        var clsName = elem[i].className;
        if (myclass.test(clsName)) {
            retnode.push(elem[i]);
        }
    }
    return retnode;
}

function getTextFromSearchWord(node, pat, pos, maxLen) {

    try {
        var contents = '';
        var startPos=0;
        var wordLen = Math.max(pat.length, maxLen);
        startPos = pos - parseInt(maxLen/2);
        if( startPos > 0 ) {
            var endPos = pat.length + (maxLen/2);
            var nodeLen = node.textContent.length;
            if( endPos > nodeLen ) {
                startPos -= endPos - nodeLen;
            }
        } else {
            startPos = 0;
        }
        var content = node.substringData(startPos, maxLen);
        return content;
    } catch(err) {
        log('getTextFromSearchWord : ' + err);
    }
    return null;
}

/**************************************************** s:move page*/
function gotoID(inputid, twoPageViewMode) {

	isPageMoveRequest = true;

	resetBodyStatus();

	if(gCurrentViewMode!=3){
		 var pageNum=0;
		    try {
		    	var checkid = '#'+inputid;

		    	var left = $(checkid)[0].offsetLeft;

		    	if( gDirectionType == 1 )
		    		left = left - getWindowWidth(twoPageViewMode);

		    	var retval = Math.abs(left);
		        pageNum = Math.floor((retval / getWindowWidth(twoPageViewMode)));
		    } catch(err) {
		        pageNum = 0;
		    }
		    goPage(pageNum, twoPageViewMode);
	} else if(gCurrentViewMode==3){

		var checkid = '#'+inputid;
		var top = $(checkid)[0].offsetTop;

		window.scrollTo(0, top);

		if($(document).height()<=window.innerHeight)
			goPage(0, twoPageViewMode);
	}
}

function gotoHighlight(inputid, twoPageViewMode) {

	resetBodyStatus();

	if(gCurrentViewMode!=3){

		var pageNum=0;

	    try {
	    	var left = $("[title="+inputid+"]").offset().left;

	    	if( gDirectionType == 1 )
	    		left = left - getWindowWidth(twoPageViewMode);

	    	var retval = Math.abs(left);
	        pageNum = Math.floor((retval / getWindowWidth(twoPageViewMode)));
	    } catch(err) {
	        pageNum = 0;
	    }
	    goPage(pageNum, twoPageViewMode);

	} else if(gCurrentViewMode==3){
		var top = $("[title="+inputid+"]").offset().top;
		window.scrollTo(0, top);
		if($(document).height()<=window.innerHeight)
			goPage(0, twoPageViewMode);
	}
}

function gotoPATH(path, isReload, twoPageViewMode) {

	resetBodyStatus();

	path = stringDotRevision(path);

	if(gCurrentViewMode!=3){
		var pageNum=0;
		try {

			var left = $(path).offset().left;

			if( gDirectionType == 1 )
				left = left - getWindowWidth(twoPageViewMode);

			var retval = Math.abs(left);
			pageNum = Math.floor((retval / getWindowWidth(twoPageViewMode)));
		}
		catch(err) {
			pageNum = 0;
		}
		goPage(pageNum, twoPageViewMode);
	} else if(gCurrentViewMode==3){
		var top = $(path).offset().top;
		window.scrollTo(0,top);
		if($(document).height()<=window.innerHeight || isReload){
			goPage(0, twoPageViewMode);
		}
	}
}

function goPage(pageNumber, twoPageViewMode) {
    return goPageScrollWithCallback(pageNumber, twoPageViewMode, null);
}

function goPageScrollWithCallback(pageNumber, twoPageViewMode, callback) {

    if (pageNumber < 0 ) {
    	pageNumber = 0;
    }

    if ( pageNumber >= gPageCount) {
    	pageNumber = gPageCount - 1;
    }

    gCurrentPage = pageNumber;
    var windowWidth = getWindowWidth(twoPageViewMode);
    gPosition = (gCurrentPage) * windowWidth;

    if( gDirectionType == 1 )
    	gPosition = -gPosition;

    var currentOffset = window.pageXOffset;

    if(gCurrentViewMode!=3){
        window.scrollTo(gPosition, 0);
    } else{
        window.selection.setPercentInChapter(window.scrollY/document.body.scrollHeight*100);
    }

    if(noteref.status()){
        noteref.hide();
        noteref.isChanged=false;
        window.selection.setAsidePopupStatus(false);
    }

    if( pageNumber == 0 || isPageMoveRequest){
        isPageMoveRequest = false;
        window.selection.updatePosition(gCurrentPage, gPosition);
    }

    return gPosition;
}
/**************************************************** e:move page*/

//function showNoteRefPopup(aTag) {
//	try{
//		if (aTag.attr("epub\:type") == "noteref") {
//
//			var href = aTag.attr("href");
//
//			for ( var i = 0; i < gNoteRefArray.length; i++) {
//				var noteRef = gNoteRefArray[i];
//
//				if (href == noteRef.href) {
//					var noteLink = noteRef.noteLinkData;
//
//					if (noteLink.tagName.toUpperCase() == "DIV" && noteLink.epubType != null) {
////						var ref = $(noteRef.href);
////						var text = noteLink.innerHTML;
////						log('ref text : ' + text);
////
////						poshytip.bind(aTag);
////						poshytip.position();
////						poshytip.text(text);
////						poshytip.show();
//						return true;
//					}
//				}
//			}
//		}
//	} catch(err) {
//		log("showNoteRefPopup : " + err);
//	}
//
//	return false;
//}

function getFirstVisibleItem(twoPageView) {

	try {
		var docscrolltop = $(document).scrollTop();
		var docscrollleft = $(document).scrollLeft();
		var viewportWidth = getWindowWidth(twoPageView);   // $(window).width();
		var viewportHeight = gWindowInnerHeight;    //$(window).height();
		var scrTop = docscrolltop;
		var scrBottom = docscrolltop + viewportHeight;
		var scrLeft = docscrollleft;
		var scrRight = docscrollleft + viewportWidth;

		var retval=null;

		var data = $('#feelingk_bookcontent').find('*');

		for(var i=0; i<data.length; i++ ) {

			var node = data[i];
    		var elementoffset = $(node).offset();

    		if ($(node).contents().filter( function(){ return this.nodeType == 3;}).text().trim() == "" &&
    				node.tagName.toUpperCase() !='IMG' &&
    				node.tagName.toUpperCase() !='VIDEO' &&
    				node.tagName.toUpperCase() !='AUDIO' &&
    				'textContent' in node ) {
    			continue;
    		}

    		if( elementoffset!=null &&
    				$(node).css("display") != "none" &&
    				$(node).is(":visible") == true &&
    				node.tagName.toUpperCase() !== 'BR' &&
    				$(node).hasClass(HIGHLIGHT_CLASS) != true &&
    				$(node).hasClass(SEARCH_HIGHLIGHT) != true) {

    			if(gCurrentViewMode == 3 ){
    				if(docscrolltop <= elementoffset.top){
    					retval = $(node).getPath();
    					break;
    				}
    			} else {
    				if(scrLeft <= elementoffset.left && elementoffset.left < scrRight ){
    					retval = $(node).getPath();
    					break;
    				}
    			}
    		}
		}

		if(retval==null){
			var pointX = parseInt($("#feelingk_booktable").css("webkit-column-width")) / 2;
			var pointY = parseInt($("#feelingk_booktable").css("margin-top")) + 1;
			var element = document.elementFromPoint(pointX, pointY);
			retval = $(element).getPath();
		}

		return retval;
	}
	catch(err) {
		log('getFirstVisibleItem() - ' + err);
	}
	return null;
}

/// Bookmark가 보여지는지에 대해서
function isBookMarkVisible(elementpath) {

	if(gCurrentViewMode==3){
		var docscrolltop = $(document).scrollTop();
		var viewportHeight = $(window).height();
		var minTop = docscrolltop;
		var maxTop = docscrolltop + viewportHeight;

		if( elementpath==null || elementpath=='' ) {
			return 0;
		}

		var retval = $(elementpath).offset().top;
		if( retval >= minTop && retval <= maxTop) {
			return 1;
		} else {
			return 0;
		}
	} else{
		var docscrollleft = $(document).scrollLeft();
		var viewportWidth = $(window).width();
		var minLeft = docscrollleft;
		var maxLeft = docscrollleft + viewportWidth;

		if( elementpath==null || elementpath=='' ) {
			return 0;
		}

		var retval = $(elementpath).offset().left;
		if( retval > minLeft && retval < maxLeft) {
			return 1;
		} else {
			return 0;
		}
	}
}

function getChapterID(Ids, twoPageViewMode) {

    var start;
    var end;

    var pages = new Array();

    for(var i=0; i<Ids.length; i++) {
        start = document.getElementById(Ids[i]);
        if( i+1 < Ids.length )
            end = document.getElementById(Ids[i+1]);

        if(end == null) end = start;

        if( start !== null && end !== null) {
            var pageObject = new Object();
            pageObject.id = Ids[i];
            pageObject.startElement = start;
            pageObject.endElement = end;
            pageObject.start = $(start).offset().left;
            pageObject.end = $(end).offset().left;
            pages.push(pageObject);
        }
    }
    return pages;
}

function innerSearch(node, Ids, keyword, maxLen, twoPageView, currFile) {

	var skip = 0;

	if( node.nodeType == 3 ) {

		var pos = node.data.toUpperCase().indexOf(keyword);

    	while( pos >= 0 ) {

    		var textSnippet = getTextFromSearchWord(node, keyword, pos, maxLen);

    		var startElement = getAncestorElementForTextNode(node);
            var startElementPath = $(startElement).getPath();

            var l = $(startElementPath).offset().left;
            var t = $(startElementPath).offset().top;

            var width = getWindowWidth(twoPageView);
            var respage = Math.floor(l / width);

            var arrPages = getChapterID(Ids, twoPageView);
            var scrLeft = respage * width;
            var scrRight = (respage+1) * width;
            var lastId = '';

            for(var i=0; i<arrPages.length; i++) {
            	var po = arrPages[i];
            	lastId = po.id;

            	if( (l >= scrLeft && l < scrRight ) &&
        			node.offsetTop >= po.startElement.offsetTop &&
        			node.offsetTop < po.endElement.offsetTop ) {
            		break;
            	}
            }

            var result = new Object();
            result.elementPath = startElementPath;
            result.text = textSnippet;
            result.page = respage;
            result.id = lastId;
            result.charOffset = pos;
            result.file = currFile;
            result.keyword = keyword;

          	window.paging.reportSearchResult(JSON.stringify(result));

            pos = node.data.toUpperCase().indexOf(keyword, pos+1);
    	}

		skip = 1;
	} else if (node.nodeType == 1 && node.childNodes && !/(script|style)/i.test(node.tagName)) {
		for(var i=0; i<node.childNodes.length; i++) {
			innerSearch( node.childNodes[i], Ids, keyword, maxLen, twoPageView, currFile);
		}
	}

	return skip;
}

function searchText(Ids,keyword,maxLen,twoPageView,currFile) {
    window.paging.reportSearchStart();

    var element = $('#feelingk_bookcontent')[0];

    for(var i=0; i<element.childNodes.length; i++) {
    	var node = element.childNodes[i];

    	var key = keyword.toUpperCase();
    	innerSearch(node, Ids, key, maxLen, twoPageView, currFile);
    }

    window.paging.reportSearchEnd(keyword);
}

function innerSearchByKeywordIndex(node, keyword, keywordIndex, twoPageView) {

	var keyIdx = keywordIndex;

	if( node.nodeType == 3 ) {

		var pos = node.data.toUpperCase().indexOf(keyword);

		while( pos >= 0 ) {

			keyIdx -= 1;

			if(keyIdx == -1){

				var startElement = getAncestorElementForTextNode(node);
	            var startElementPath = $(startElement).getPath();

	            var l = $(startElementPath).offset().left;

				var spannode = document.createElement('span');
		        spannode.className = SEARCH_HIGHLIGHT;
		        var middlebit = node.splitText(pos);
		        var endbit = middlebit.splitText(keyword.length);
		        var middleclone = middlebit.cloneNode(true);
		        spannode.appendChild(middleclone);
		        middlebit.parentNode.replaceChild(spannode, middlebit);

		        var spanPath = $(spannode).getPath();

			    var left = $(spannode).offset().left;

			    var width = getWindowWidth(twoPageView);

			    if( gDirectionType == 1 )
			    	left = left - width;

			    left = Math.abs(left);

	            var respage = Math.floor(left / width);

			    log('$(spannode).offset().left : ' + left);

		        var rects = getSpanElementRects(spannode, left);

		        var result = new Object();
		        result.bounds = rects;
		        result.page = respage;
		        result.text = keyword;

		        $('#feelingk_bookcontent').removeHighlight();

		        log("innerSearchByKeywordIndex() : "+JSON.stringify(result));

		        if(gCurrentViewMode==3)
		        	window.scrollTo(0,rects[0].top);

	            window.selection.reportFocusRect(JSON.stringify(result));

				return keyIdx;
			}
			pos = node.data.toUpperCase().indexOf(keyword, pos+1);
		}
	} else if (node.nodeType == 1 && node.childNodes && !/(script|style)/i.test(node.tagName)) {
		for(var i=0; i<node.childNodes.length; i++) {
			var result = innerSearchByKeywordIndex( node.childNodes[i], keyword, keyIdx, twoPageView);
			keyIdx = result;
			if(result == -1){
				return keyIdx;
			}
		}
	}
	return keyIdx;
}

function searchTextByKeywordIndex(highlights, keyword,keywordIndex,twoPageView) {

	var element = $('#feelingk_bookcontent')[0];

	deleteAllHighlights(highlights);

	var keyIdx = keywordIndex;

	for(var i=0; i<element.childNodes.length; i++) {
		var node = element.childNodes[i];

		var key = keyword.toUpperCase();
		var result = innerSearchByKeywordIndex(node, key, keyIdx, twoPageView);

		if(result == -1){
			break;
		}

		keyIdx = result;
	}

	if( keywordIndex == keyIdx ){
		window.selection.reportFocusRect(JSON.stringify(""));
	}
	log("search end");
	applyHighlights(highlights);
}

function searchRect(path, keyword, offset) {

	path = stringDotRevision(path);

    window.paging.reportFocusRectStart();

	try {
		var node = $(path)[0];
		getRectForSearchResult(node, keyword.toUpperCase(), offset);
	} catch(err) {
		log('searchRect: ' + err);
	}
    window.paging.reportFocusRectEnd();
}

function getRectForSearchResult(node, keyword, offset) {

	var skip = 0;

	if( node.nodeType == TEXT_NODE ) {

		var pos = node.data.toUpperCase().indexOf(keyword, offset);

		if (pos >= 0 ) {

	        var spannode = document.createElement('span');
	        spannode.className = SEARCH_HIGHLIGHT;
	        var middlebit = node.splitText(pos);
	        var endbit = middlebit.splitText(keyword.length);
	        var middleclone = middlebit.cloneNode(true);
	        spannode.appendChild(middleclone);
	        middlebit.parentNode.replaceChild(spannode, middlebit);

	        var spanPath = $(spannode).getPath();

		    var l = $(spannode).offset().left;
		    var t = $(spannode).offset().top;

	        var rects = getSpanClientRects(spannode);

	        var result = new Object();
	        result.offset = offset;
	        result.pos = pos;
	        result.bounds = rects;

	        window.paging.reportFocusRect(JSON.stringify(result));
		}

		skip = 1;
	}
	else if (node.nodeType == 1 && node.childNodes && !/(script|style)/i.test(node.tagName)) {
        for (var i = 0; i < node.childNodes.length; ++i) {
            i+=getRectForSearchResult(node.childNodes[i], keyword, offset);
        }

    }

	return skip;
}

function removeSearchHighlight(){
	$('#feelingk_bookcontent').removeHighlight();
}

function getBookmarkPath(Ids, bookMarks, twoPageView) {

	for(var i=0 ;i<bookMarks.length; i++) {
		// bookmark is aleady visible
		if( isBookMarkVisible( bookMarks[i] ) == 1 ) {
	        window.selection.reportBookmarkPath(bookMarks[i], true);
	        return;
		}
	}

    var pathObj = new Object();

    var elementPath= getFirstVisibleItem(twoPageView);

    if( elementPath !== null ) {

        var retval = $(elementPath).offset().left;
    	var respage = Math.floor((retval / window.innerWidth));

    	var arrPages = getChapterID(Ids, twoPageView);
    	var position = respage * gRealHeight * getNumColumns(twoPageView);
        var lastId = '';

        for(var i=0; i<arrPages.length; i++) {

            var po = arrPages[i];
            lastId = po.id;

            if( position > po.start && position < po.end ) {
                // 현재 북마크가 start id 와 end id 사이에 있는 경우
                // end id 가 보이면 end id의 path로 설정
                if( isBookMarkVisible(po.endElement) == 1 ) {
                    lastId = po.endElement.id;
                    elementPath = $(po.endElement).getPath();
                }
                break;
            } else if( po.start >= position ) {
                if( isBookMarkVisible(po.startElement) == 0 ) {
                    lastId = '';
                }
                break;
            }
        }

    	var node = $(elementPath)[0];
        var snippet = '';
        var tagName="";
        var isText = false;

    	if( node.textContent==undefined ) {
    		var child = node.childNodes[0];
    		if( child.tagName.toUpperCase() =='IMG' ) {
    			tagName = child.tagName;
    			snippet = decodeURI(child.src);
    		} else if(child.tagName.toUpperCase() =='AUDIO') {
    			tagName = child.tagName;
    			snippet = decodeURI(child.src);
    		}  else if(child.tagName.toUpperCase() =='VIDEO') {
    			tagName = child.tagName;
    			snippet = decodeURI(child.src);
    		}
    	} else {
    		if( node.tagName.toUpperCase() == 'IMG' ) {
    			tagName = node.tagName;
    			snippet = decodeURI(node.src);
    		}  else if(node.tagName.toUpperCase() =='AUDIO') {
    			tagName = node.tagName;
    			snippet = decodeURI(node.src);
    		} else if(node.tagName.toUpperCase() =='VIDEO') {
    			tagName = node.tagName;
    			snippet = decodeURI(node.src);
    		} else {
    			isText=true;
    			snippet = node.textContent;
    		}
    	}

    	if( isText && snippet.length > 100 )
    		snippet = snippet.substring(0, 100);

    	pathObj.id = lastId;
    	pathObj.page = respage;
    	pathObj.elementPath = elementPath;
    	pathObj.elementText = snippet;
    	pathObj.tagName = tagName;

        window.selection.reportBookmarkPath(JSON.stringify(pathObj), false);
    } else {
    	window.selection.reportError(2);
        window.selection.reportBookmarkPath(null, false);
    }
}

function getBookmarkForPage(bookMarks) {

    for(var i=0; i<bookMarks.length; i++) {

        var elementPath = bookMarks[i];
        if( isBookMarkVisible(elementPath) == 1 ) {
            window.selection.reportBookmarkForPage(true);
            return;
        }
    }
    window.selection.reportBookmarkForPage(false);
}

function changeFontDirect(faceName, fontPath) {

	if (faceName == null || fontPath == null ) {
        faceName = "";
        fontPath = "";
    }

    if (!(faceName == "" || fontPath == "")){

        // 동일 font-face가 계속 생성되지 않도록 예외처리
        if ($("#FONTFACE_" + faceName).length <= 0) {

        	// style 엘리먼트에 faceName을 기준으로 ID를 생성한다.
        	$("head").prepend("<style type='text/css' id='FONTFACE_" + faceName +"'>" +
        			"@font-face {\n" +
        			"\tfont-family: '" + faceName + "';\n" +
        			"\tsrc: url('" + fontPath + "');\n" +
        			"}\n" +
        	"</style>");
        }
    } else {
    	// faceName과 fontPath가 null 이나 "" 일 경우 원본 폰트로 설정하기 위해 설정된 font-face를 삭제
    	// FONTFACE로 시작하는 ID를 가진 모든 style 엘리먼트를 삭제한다.
    	$("style[id^='FONTFACE']").remove();
    }

    $('*').each(function () { this.style.setProperty('font-family', faceName, 'important'); });
}

//function setListStyleType(styleType) {
//    $("ul,ol,dl").css("list-style-type", styleType);
//}

function setFont(fontFamily) {
	log("setFont only family");
    $("*").css('font-family', '"' + fontFamily + '"');
}

function setFont(fontFamily, fontPath) {
	log("setFont 2para");
	$("head").prepend("<style type='text/css'>" +
			"@font-face {\n" +
			"\tfont-family: '" + fontFamily + "';\n" +
			"\tsrc: url('" + fontPath + "');\n" +
			"}\n" +
	"</style>");

	$("*").css('font-family', '"' + fontFamily + '"');
}

//function setFontStyle(fontStyle) {
//    $("*").css('font-style', fontStyle);
//}
//
//function setFontWeight(fontWeight) {
//    $("*").css('font-weight', fontWeight);
//}

//function setTextEmphasis(style, color) {
//   var value = style + ' ' + color;
//   $('*').filter(function () { return $(this).css('webkitTextEmphasisStyle')!='none'}).css('-webkit-text-emphasis', value);
//}

function changeFontSizeDirect(value) {
	log('changeFontSizeDirect :' + value);
	$('#feelingk_bookcontent').css('font-size', value, 'important');
}

function changeLineHeightDirect(value) {
	$('p,div,span').each(function(){
		this.style.setProperty('line-height', value, 'important')
	});
}

function changeBackgroundColorDirect(color,nightMode) {

	if(nightMode) {
		$('body').find('a').css('color','#6887f7', 'important');			// 야간모드 a tag 색상 변경
		$('body').find('*').not('a').css('color', '#bebebe', 'important');	// 야간모드 폰트 색상 변경
		$('body').find('*').css('backgroundColor', color, 'important');		// 야간모드 배경 색상 변경
		$('body').css('color', '#bebebe', 'important');
		$('body').css('backgroundColor', color, 'important');
	} else {
		$('body').find('*').css('color', '', 'important');
		$('body').find('*').not('a').css('color', '#000000', 'important');
		$('body').find('*').css('backgroundColor', '', 'important');
		$('body').find('*').css('backgroundColor', color, 'important');
		$('body').css('color', '#000000', 'important');

		if(color.toUpperCase()!="#FFFFFF"){
			$('body').css('backgroundColor', color, 'important');	// white인 경우는 원본스타일로 본다
		}
	}
	$('#feelingk_booktable').css('backgroundColor', color);
}

function changeParaHeightDirect(value) {
	$('p,div').each(function(){this.style.setProperty('margin-bottom', value, 'important')});
}

function changeMarginDirect(left,top,right,bottom) {
	var height = gWindowInnerHeight - (top+bottom);
	var bc = document.getElementById('feelingk_bookcontent');
	$('#feelingk_bookcontent').css('marginLeft', left);
	$('#feelingk_bookcontent').css('marginRight', right);
	$('#feelingk_booktable').css('marginTop', top);
	$('#feelingk_booktable').css('height', height);
}

function changeIndentDirect(indent) {

	var str;
	if( indent )
		str = '1em';
	else
		str = '0em';

	$('p, div').each(function(){this.style.setProperty('text-indent', str, 'important')});
}

function changeHighlightColorDirect(highlightID, clrIndex, callBack) {  // TODO :: new custom selection - deleted

	try {

        var highlightSpans=$("[title=\""+highlightID+"\"]");

        for(var i=0; i<highlightSpans.length; i++) {
            var span = highlightSpans[i];

            var className = $(span).attr('class');
            $(span).removeClass('FLKAnnotationColor0 FLKAnnotationColor1 FLKAnnotationColor2 FLKAnnotationColor3 FLKAnnotationColor4 FLKAnnotationColor5');
            $(span).addClass(HIGHLIGHT_CLASS);
            $(span).addClass('FLKAnnotationColor'+clrIndex);

            if(i==0){	// 색상 변경 시 기존 데이터 percent update
                percent = getPercentOfElement($(span));
            }
        }

        if(callBack)
            window.selection.changeHighlightColor(highlightID, clrIndex, percent);

    } catch(err) {
        log("changeHighlightColorDirect err : "+err);
    }
}

//function addCSSClass(className, classRule) {
//	if (document.all) {
//		document.styleSheets[0].addRule("." + className, classRule)
//	} else if (document.getElementById) {
//		document.styleSheets[0].insertRule("." + className + " { " + classRule + " }", 0);
//	}
//}

/**************************************************** s:TTS */
function getPathOfFirstElement(ttsDataJsonArr) {

	var twoPageViewMode = 0;
	if( gColumnWidth != $('#feelingk_booktable').css('width') )
		twoPageViewMode = 1;

	var currentXPath = getFirstTTSElementPath(twoPageViewMode, ttsDataJsonArr);

	if( currentXPath == null || currentXPath == "" )
		window.ttsDataInfo.setTTSCurrentIndex(null);
	else
		window.ttsDataInfo.setTTSCurrentIndex(currentXPath);
}

function getFirstTTSElementPath(twoPageViewMode, ttsDataJsonArr) {

	var docscrollleft = $(document).scrollLeft();
	var viewportWidth = getWindowWidth(twoPageViewMode);
	var scrLeft = docscrollleft;
	var scrRight = docscrollleft + viewportWidth;
	var docscrolltop = $(document).scrollTop();

	if( ttsDataJsonArr.length > 0 ) {

		for ( var i = 0; i < ttsDataJsonArr.length; i++) {

			if ($(ttsDataJsonArr[i]).css('display') == "none")
                continue;

			if(gCurrentViewMode==3){
				var offsetTop = $(ttsDataJsonArr[i]).offset().top;
				if( offsetTop >= docscrolltop){
					log("currentXPath : "+ttsDataJsonArr[i]);
					return ttsDataJsonArr[i];
				}
			} else {
				var offsetLeft = $(ttsDataJsonArr[i]).offset().left;
				if( offsetLeft >= scrLeft && offsetLeft < scrRight ){
					log("currentXPath : "+ttsDataJsonArr[i]);
					return ttsDataJsonArr[i];
				}
			}
		}

		 var pointX = parseInt(gColumnWidth) / 2;
	     var pointY = parseInt(gMarginTop) + 1;
	     var element = document.elementFromPoint(pointX, pointX);
	     if (element.tagName.toLowerCase() == "html" ||
	         element.tagName.toLowerCase() == "body" ||
	         element.id == "feelingk_booktable" ||
	         element.id == "feelingk_bookcontent")
	         return null;

	     while (!(/(div|section|figure)/i.test(element.parentElement.tagName.toLowerCase()))
	    		 && element.id != "feelingk_bookcontent") {
	         element = element.parentElement;
	     }
	     return getTTSPath(element);
	} else {
		return null;
	}
}

function getTTSPath(element) {

    var path ="";
    var node = element;

    while (node && node.localName.toLowerCase() != "html") {
        var name = node.localName.toLowerCase();
        if (!name) break;
        var parent = node.parentElement;
        var siblings = $(parent).children(name);
        name += ":eq(" + siblings.index(node) + ")";
        path = name + (path ? ">" + path: "");
        node = parent;
    }
    return path;
}

//function getSelectedElementPath(startElement, endElement){  // TODO :: new custom selection - deleted
//	var startElementPath = getTTSPath($(startElement)[0]);
//	var endElementPath = getTTSPath($(endElement)[0]);
//	window.ttsDataInfo.setSelectedElementPath(startElementPath, endElementPath);
//}

function getSelectedElementPath(){  // TODO :: new custom selection - added

    if(totalRange == undefined || totalRange == null || totalRange.toString().length==0)
        return;

    var currentSelectionInfo = requestAnnotationInfo(totalRange, true);
    var startElementPath = getTTSPath($(currentSelectionInfo.startElementPath)[0]);
    var endElementPath = getTTSPath($(currentSelectionInfo.endElementPath)[0]);
    var startCharOffset = currentSelectionInfo.startCharOffset;
    var endCharOffset = currentSelectionInfo.endCharOffset;

	window.ttsDataInfo.setSelectedElementPath(startElementPath, endElementPath, startCharOffset, endCharOffset);
}

function getFirstSentence(ttsDataArray, path, listStartIndex) {

	var twoPageViewMode = 0;
	if( $('#feelingk_booktable').css('-webkit-column-width') != $('#feelingk_booktable').css('width') )
		twoPageViewMode = 1;

	path = path.replace(/\./g, "\\.");

	var el = $(path)[0];
	var elements = [el];

    if (el.tagName.toLowerCase() == "img" || el.tagName.toLowerCase() == "video" || el.tagName.toLowerCase() == "audio"
    || el.tagName.toLowerCase() == "svg" || el.tagName.toLowerCase() == "math") {
        var clientRects = el.getClientRects();
        if (clientRects[0].left >= 0 && clientRects[0].left < getWindowWidth(twoPageViewMode)) {
            window.ttsDataInfo.setStartPosition(path, i, listStartIndex);
            return;
        }
    }

	for( var i = 0; i < ttsDataArray.length; i++ ) {
		var start = ttsDataArray[i].start;
		var end = ttsDataArray[i].end;

		var ranges = [ start, end ];
		var range = document.createRange();

		log("getFirstSentence textArray[i] : " + ttsDataArray[i].text);
		log("getFirstSentence start : " + start);
		log("getFirstSentence end : " + end);

		getCharElement(elements, ranges);
		log("getFirstSentence ranges : " + ranges[0].count);
		range.setStart(ranges[0].el, ranges[0].count);
		log("getFirstSentence ranges : " + ranges[1].count);
	    range.setEnd(ranges[1].el, ranges[1].count);

	    var clientRects = range.getClientRects();

	    if( clientRects.length > 0 ) {
		    if( clientRects[0].left >= 0 && clientRects[0].left < getWindowWidth(twoPageViewMode)  ){
		    	window.ttsDataInfo.setStartPosition(path, i, listStartIndex);
		    	return;
		    }
	    }
	}
	window.ttsDataInfo.setStartPosition(path, 0, listStartIndex);
}

function applyTTSHighlight(highlight) {
    try {
    	log("applyHighlight" );
        var startElement=getNodeFromElementPath(highlight.startElementPath, 0);
        var endElement=getNodeFromElementPath(highlight.endElementPath, 0);

        if(startElement===null) {
            log("No highlight start element");
        } else if(endElement===null) {
            log("No highlight end element");
        } else {
            var range=document.createRange();
            if(setPointInRange(startElement,highlight.startCharOffset,range,setRangeStart)) {
                if(setPointInRange(endElement,highlight.endCharOffset,range,setRangeEnd)) {
                    highlightRange(range,highlight.isDeleted,highlight.isAnnotation,highlight.highlightID,highlight.colorIndex);
                } else {
                    log("Could not set end of selection range");
                }
            } else {
                log("Could not set start of selection range");
            }
        }
    } catch(err) {
        log("applyHighlight: "+err);
    }
}

function removeTTSHighlight() {
	var highlightSpans=$('.'+TTS_HIGHLIGHT_CLASS);
	if( highlightSpans.length > 0 ) {
    	$(highlightSpans).contents().unwrap();
    }
}

function setTTSHighlight(ttsData) {

	var currentNode = $(ttsData.path)[0];

	if (currentNode.tagName.toLowerCase() == "img" || currentNode.tagName.toLowerCase() == "video" || currentNode.tagName.toLowerCase() == "audio"
		  || currentNode.tagName.toLowerCase() == "svg" || currentNode.tagName.toLowerCase() == "math") {
        var clientRects = currentNode.getClientRects();
        var rects = new Array();
        var rect = getRectangleObject(document.body.scrollLeft + clientRects[0].left, document.body.scrollTop+clientRects[0].top, clientRects[0].width, clientRects[0].height);
        if (rect != null)
            rects.push(rect);
        var result = new Object();
        result.bounds = rects;
        result.filePath = ttsData.filePath;

        var nextPage = false;
    	var leftVal = clientRects[0].left;
    	var twoPageViewMode = 0;
    	if( $('#feelingk_booktable').css('-webkit-column-width') != $('#feelingk_booktable').css('width') )
    		twoPageViewMode = 1;

    	if( leftVal > getWindowWidth(twoPageViewMode)  ){
	    	nextPage = true;
	    }

    	if(gCurrentViewMode==3){
    			var $target = $('body');
            if(clientRects[0].top > window.innerHeight*0.25 || clientRects[0].top<0){
    			$target.animate({scrollTop: (document.body.scrollTop+clientRects[0].top-window.innerHeight*0.25)}, 500);
    		}
    	}
    	window.highlighter.requestHighlightRect(JSON.stringify(result), nextPage);
        return;
    }

	log('start : ' + ttsData.start);
	log('end : ' + ttsData.end);
	var ranges = [ ttsData.start, ttsData.end ];
	var range = document.createRange();

	var elements = [currentNode];

	getCharElement(elements, ranges);

	range.setStart(ranges[0].el, ranges[0].count);
    range.setEnd(ranges[1].el, ranges[1].count);

    var notSame = false;
    if( (ranges[0].el.parentElement == ranges[1].el.parentElement.parentElement) ||( ranges[0].el != ranges[1].el && ranges[0].el.parentElement.parentElement == ranges[1].el.parentElement.parentElement) )
    	notSame = true;

    var endElementRects = ranges[1].el.parentElement.getClientRects();
    var endRects = [];
    for( var i = 0 ; i < endElementRects.length; i++ ) {
    	endRects.push(endElementRects[i]);
    }

    var clientRects = range.getClientRects();

    var rects = new Array();
    for( var i=0; i < clientRects.length; i++ ){
    	var clientRect = clientRects[i];
        if(gCurrentViewMode==3)
            rects.push( getRectangleObject(document.body.scrollLeft + clientRect.left, document.body.scrollTop+clientRect.top, clientRect.width, clientRect.height) );
        else
            rects.push( getRectangleObject(document.body.scrollLeft + clientRect.left, clientRect.top, clientRect.width, clientRect.height) );
	}

	var result = new Object();
	result.bounds = rects;
    result.filePath = ttsData.filePath;

	var pageNum=0;

    if(gCurrentViewMode==3){
   		var $target = $('body');
        if(clientRects[0].top > window.innerHeight*0.25 || clientRects[0].top<0){
    		$target.animate({scrollTop: (document.body.scrollTop+clientRects[0].top-window.innerHeight*0.25)}, 500);
    	}
    } else {
        try {
            var nextPage = false;
            var leftVal = clientRects[0].left;
            var twoPageViewMode = 0;
            if( $('#feelingk_booktable').css('-webkit-column-width') != $('#feelingk_booktable').css('width') )
                twoPageViewMode = 1;

            if(clientRects[0].left < 0){
                isPageMoveRequest = true;
                var left = $(ttsData.path).offset().left;

                if( gDirectionType == 1 )
                    left = left - getWindowWidth(twoPageViewMode);

                var retval = Math.abs(left);
                pageNum = Math.floor((retval / getWindowWidth(gTwoPageViewMode)));
                goPage(pageNum, gTwoPageViewMode);
            } else if( clientRects[0].left > getWindowWidth(twoPageViewMode)){
                nextPage = true;
            }
            pageNum = Math.floor((leftVal / getWindowWidth(twoPageViewMode)));
        } catch(err) {
            log("setTTSHighlight err :  : "+err);
            pageNum = 0;
        }
    }
    window.highlighter.requestHighlightRect(JSON.stringify(result), nextPage);
}
/**************************************************** e:TTS */

function getCharElement(elems, range, len) {
    var elem, start;
    len = len || 0;
    for (var i = 0; i<elems.length; i++) {
        elem = elems[i];
        if (elem.nodeType === 3 || elem.nodeType === 4) {
            start = len;
            len += elem.nodeValue.length;
            replaceWithLess(start, len, range, elem);
        } else if (elem.nodeType !== 8) {
            len = getCharElement(elem.childNodes, range, len);
        }
    }
    return len;
}

function replaceWithLess(start, len, range, el) {
    if (typeof range[0] === 'number' && range[0] < len) {
        range[0] = {
            el: el,
            count: range[0] - start
        };
    }
    if (typeof range[1] === 'number' && range[1] <= len) {
        range[1] = {
            el: el,
            count: range[1] - start
        };
        ;
    }
}

/** 현재 챕터 element id list 구하는 함수 */
function getIDList(filePath) {

    var result = $("#feelingk_bookcontent *").map(function(index) {
    	return this.id;
    });

    var idList = [];
    for (var i=0; i<result.length; ++i){
    	if (result[i] != ""){
    		idList.push(result[i]);
    	}
    }
    window.mediaoverlay.setIdList(JSON.stringify(idList), filePath);
}

function getIDListByPoint(x, y, filePath) {

	var element=document.elementFromPoint(x, y);
    var idList = new Array();

    while (element.id != "feelingk_bookcontent" && element.tagName.toLowerCase() != "body"){
    	if (element.id != "" && element.id != undefined) {
    		idList.push(filePath + "#" + element.id);
    	}
    	element = element.parentElement;
    	if(element==null)
    		break;
    }
    log("getIDListByPoint json : "+JSON.stringify(idList));
    window.selection.setIdListByPoint(JSON.stringify(idList));
}

/** element id list 중 현재 페이지 첫번째 element 구하는 함수 */
function getIDofFirstVisibleElement(filePath, ids){

    var docscrollleft = $(document).scrollLeft();
    var viewportWidth = $(window).width();
    var scrLeft = docscrollleft;
    var scrRight = docscrollleft + viewportWidth;

    var scrollTop = $(document).scrollTop();

    if(gCurrentViewMode!=3){
    	 for (var i=0; i<ids.length; ++i) {
    	        var node = $("#" + ids[i])[0];
    	        var leftOffset = $(node).offset().left;
    	        if (leftOffset >= scrLeft && leftOffset <= scrRight){
    	        	window.mediaoverlay.setIDofFirstVisibleElement(filePath, ids[i]);
    	        	break;
    	        }
    	 }
    } else if(gCurrentViewMode==3){
    	for (var i=0; i<ids.length; ++i) {
    		 var node = $("#" + ids[i])[0];
    		 var topOffset = $(node).offset().top;
    		  if (topOffset >= scrollTop){
  	        	window.mediaoverlay.setIDofFirstVisibleElement(filePath, ids[i]);
  	        	break;
  	        }
    	}
    }
}

/** element id로 rect 구하는 함수 */
//function getRectOfElement(id) {
//
//    var node = $("#"+id)[0];
//    var rects = new Array();
//    var clientRects = node.getClientRects();
//    for (var i=0; i<clientRects.length; ++i) {
//        var rect = getRectangleObject(document.body.scrollLeft+clientRects[i].left, clientRects[i].top+document.body.scrollTop, clientRects[i].width, clientRects[i].height, rects);
//        if (rect != null)
//            rects.push(rect);
//    }
//    var result = new Object();
//    result.bounds = rects;
//    try {
//    	var pageNum=0;
//    	var nextPage = false;
//    	var leftVal = clientRects[0].left;
//    	var twoPageViewMode = 0;
//    	if( $('#feelingk_booktable').css('-webkit-column-width') != $('#feelingk_booktable').css('width')){
//    		twoPageViewMode = 1;
//    	}
//    	if( clientRects[0].left >= getWindowWidth(twoPageViewMode)  ){
//	    	nextPage = true;
//	    }
//    	pageNum = Math.floor((leftVal / getWindowWidth(twoPageViewMode)));
//    } catch(err) {
//    	log("getRectOfElement err : " + err);
//    	pageNum = 0;
//    }
//
//    if(gCurrentViewMode==3)
//    	window.scrollTo(0,document.body.scrollTop+clientRects[0].top);
//
//    window.highlighter.requestHighlightRect(JSON.stringify(result), nextPage);
//}

function addMediaOverlayHighlight(id, activeclass, playbackclass) {

    $('#'+id).addClass(activeclass);

    if(playbackclass!=""){
		$('#feelingk_bookcontent').find('*').addClass(playbackclass);
		$('#'+id).removeClass(playbackclass);
	}

    var viewPortWidth = window.innerWidth;
    var scrLeft = $(document).scrollLeft();
    var scrRight = scrLeft + viewPortWidth;

    var elementWidth = $('#'+id).width();
    var leftOffset =  $('#'+id).offset().left;
    var rightOffset = leftOffset+elementWidth;

    if(gCurrentViewMode != 3){
    	if(leftOffset >= scrLeft && rightOffset <= scrRight){
        } else{
        	window.selection.scrollNextPage();
        }
    } else if(gCurrentViewMode == 3){
    	window.scrollTo(0,$('#'+id).offset().top);
    }
}

function removeMediaOverlayHighlight(activeclass, playbackclass){
	$('#feelingk_bookcontent').find('*').removeClass(activeclass);
	$('#feelingk_bookcontent').find('*').removeClass(playbackclass);
}

//[ssin-audio] s
function getAudioContents() {

	log("getAudioContents() in");

	var audios = $("audio");

	var results = new Array();
	    
	for (var i=0; i<audios.length; ++i) {
		var audio = audios[i];
		var audioInfo = new Object();
		audioInfo.xpath = $(audio).getPath();
		if ($(audio).attr("src") != undefined) {
			audioInfo.source = $(audio).attr("src");
		} else {
			var sources = audio.querySelectorAll('source');
			if (sources.length > 0) {
				var source = sources[0];
				audioInfo.source = $(source).attr("src");
			}
		}
		audioInfo.autoplay = $(audio).attr("autoplay");
		audioInfo.loop = $(audio).attr("loop");
		audioInfo.controls = $(audio).attr("controls");
		audioInfo.muted = $(audio).attr("muted");
		audioInfo.preload = $(audio).attr("preload");
		results.push(audioInfo);
	}
	window.selection.createAudioContents(JSON.stringify(results));
}

function moveAudioPlayingPosition(xPath, movingUnit) {
	try{
    	var audio = $(xPath)[0];
    	if (audio != undefined && audio.paused == false){
            audio.currentTime = movingUnit+audio.currentTime;
    	}
    } catch(err){
    	log("Error moveAudioPlayingPosition : "+err);
    }
}

function playAudioAtTime(xPath, startTime) {

	// 기본 play는 startTime에 0
    try{
    	var audio = $(xPath)[0];
    	if (audio != undefined){
            if (startTime != 0){
            	audio.play();
            	audio.pause();
                setTimeout(function () {
                	 audio.currentTime = startTime;
               }, 200);
            }
            audio.play();
        }
    } catch(err){
    	log("Error playAudioAtTime : "+err);
    }
}

function stopAudio(xPath) {
	try {
    	var audio = $(xPath)[0];
		if (audio != undefined) {
			audio.currentTime = 0;
			audio.pause();
		}
		var audios = $('audio');
	} catch(err){
		log("Error stopAudio : "+err);
	}
}

function pauseAudio(xPath) {
	try {
    	var audio = $(xPath)[0];
		if (audio != undefined) {
			audio.pause();
		}
	} catch(err){
		log("Error pauseAudio : "+err);
	}
}

function setLoopAudio(xPath, isLoop) {
	var audio = $(xPath)[0];
	if (audio != undefined) {
        audio.loop = isLoop;
    }
}

function findAudioContentOnCurrentPage() {

	var audioElements = $("audio");
    var results = new Array();

    for (var i=0; i<audioElements.length; ++i) {

        var audioElement = audioElements[i];
        var posLeft = $(audioElement).offset().left - $(document).scrollLeft();
        var posTop = $(audioElement).offset().top;
        var posRight = posLeft + $(audioElement).width();
        var posBottom = posTop + $(audioElement).height();
        var scrollTop = $(document).scrollTop();
		var scrollBottom = $(document).scrollTop() + gWindowInnerHeight;

        if(gCurrentViewMode == 3 ){
        	if(posTop > scrollTop && posBottom < scrollBottom){
        		var id = $(audioElement).getPath();
        		if (id != undefined)
        			results.push(id);
        	}
        } else{
        	 if(posLeft > 0 && posRight < window.innerWidth){
        		 var id = $(audioElement).getPath();
                 if (id != undefined)
                     results.push(id);
             }
        }
    }
    window.audioplayer.getAudioContentsOnCurrentPage(JSON.stringify(results));
}

function audioTimer(audio) {
    window.selection.updateCurrentPlayingPosition($(audio).getPath(), audio.currentTime);
}

var isPreventMediaControl=false;
function setPreventMediaControl(isPrevent){

	isPreventMediaControl = isPrevent;

	if(isPreventMediaControl){

		var audios = $('audio');
		var videos = $('video');

		for(var i = 0; i < audios.length; i++) {
			audios[i].pause();
		}

		for(var i = 0; i < videos.length; i++) {
			videos[i].pause();
		}
	}
}

function playVideoFullScreen(videoElement){
	var valueSRC="";

	if(videoElement.src!=null && videoElement.src.length>0)
		valueSRC = videoElement.src;

	for(var i = 0; i < videoElement.children.length; i++) {
		if(videoElement.children[i].src.length > 0) {
			valueSRC = videoElement.children[i].src;
			if( videoElement.children[i].src.indexOf(".mp4") > -1 ){
				break;
			}
		}
	}

	if(currentAndroidVersion.charAt(0)>=7){
		window.selection.videocontrol(valueSRC);	// 누가 예외처리
	} else{
		window.selection.playVideo(valueSRC);
	}
}

function getAndroidOsVersion() {
    var userAgent = navigator.userAgent.toLowerCase();
    var check = userAgent.match(/android\s([0-9\.]*)/);
    return check ? check[1] : false;
}
//[ssin-audio] e

//[ssin-scroll] s
function scrollToBottom(){
	var $target = $('body');
	$target.animate({scrollTop: $('#feelingk_bookcontent').height() + window.innerHeight}, 500, function(){
		window.selection.finishScrollToBottom(window.scrollY/document.body.scrollHeight*100);
	});
}

function scrollByPercent(percent, twoPageViewMode){

	resetBodyStatus();

	window.scrollTo(0,$('#feelingk_bookcontent').height()*percent/100);
	if($(document).height()<=window.innerHeight || percent<=0)
		goPage(0,twoPageViewMode);
}
//[ssin-scroll] e

function getIDListOnCurrentPage(filePath) {

	var result = $("#feelingk_bookcontent *").map(function(index) {
		return this.id;
	});

    var viewPortWidth = window.innerWidth;
    var scrLeft = $(document).scrollLeft();
    var scrRight = scrLeft + viewPortWidth;
    var viewPortHeight = window.innerHeight;
    var scrTop = $(document).scrollTop();
    var scrBottom = scrTop + window.innerHeight;
    var idList = new Array();
    if(gCurrentViewMode != 3){
    	for (var i=0; i<result.length; ++i) {
    		if (result[i] != "") {
    			var el = $("#" + result[i])[0];
    			var leftOffset = $(el).offset().left;
    			if (leftOffset >= scrLeft && leftOffset <= scrRight){
    				idList.push(filePath+"#"+result[i]);
    			}
    		}
    	}
    } else if(gCurrentViewMode == 3) {
    	for (var i=0; i<result.length; ++i) {
    		if (result[i] != "") {
				var el = $("#" + result[i])[0];
				var topOffset = $(el).offset().top;
				if (topOffset >= scrTop && topOffset <= scrBottom) {
					idList.push(filePath+"#"+result[i]);
				}
			}
		}
	}
	log("getIDListOnCurrentPage : "+JSON.stringify(idList));
	window.selection.setIDListOnCurrentPage(JSON.stringify(idList));
}

//function isChildOfInner(element) {
//	while (element != null) {
//		if (element.className == "flk_inner")
//			return true;
//		element = element.parentNode;
//	}
//	return false;
//}

function resetBodyStatus(){
	noteref.hide();
	noteref.isChanged=false;
	window.selection.setAsidePopupStatus(false);
	if(gCurrentViewMode==3){
		var bodyEl = $('body')[0];
		bodyEl.style.position = "static";
		bodyEl.style.top = "0px";
		window.scrollTo(0,noteref.scrollTop);
	}
}

function setPreventNoteref(isPrevent){
	noteref.setPrevent(isPrevent);
}

function getPercentOfElement(element) {

    var elementTop = element.offset().top;
    var elementLeft = element.offset().left;

    if (gCurrentViewMode==3) {
    	 var pageHeight = window.innerHeight - parseInt($("#feelingk_booktable").css("margin-top")) - parseInt($("#feelingk_booktable").css("margin-bottom"));
         var totalHeight = $("#feelingk_booktable")[0].scrollHeight;

         if (totalHeight % pageHeight != 0) {
             totalHeight = pageHeight * (Math.ceil(totalHeight / pageHeight));
         }

         var totalRect = $("#feelingk_booktable").width() * totalHeight;
         var top = elementTop + $("body").scrollTop() - parseInt($("#feelingk_booktable").css("margin-top"));
         var left = elementLeft;
         var topRect = top * $("#feelingk_booktable").width();
         var rect = topRect + left;

         var percent = rect / totalRect * 100;
         return percent;
    } else {
    	var totalRect = $(document).width() * $("#feelingk_booktable").height();
        var prePageRect = $(document).scrollLeft() * $("#feelingk_booktable").height();
        var top = elementTop - parseInt($("#feelingk_booktable").css("margin-top"));
        var left = elementLeft;
        var topRect = top * $("#feelingk_booktable").width();
        var rect = prePageRect + topRect + left;

        var percent = rect / totalRect * 100;
        return percent;
    }
    return -1;
}

function getPercentOfRange(range) {

	var clientRect = range.getClientRects()[0];

    var body = $('body')[0];

    if (clientRect) {
        if (gCurrentViewMode==3) {
            var pageHeight = window.innerHeight - parseInt($("#feelingk_booktable").css("margin-top")) - parseInt($("#feelingk_booktable").css("margin-bottom"));
            var totalHeight = $("#feelingk_booktable")[0].scrollHeight;
            if (totalHeight % pageHeight != 0) {
                totalHeight = pageHeight * (Math.ceil(totalHeight / pageHeight));
            }
            var totalRect = $("#feelingk_booktable").width() * totalHeight;
            var top = clientRect.top + $("body").scrollTop() - parseInt($("#feelingk_booktable").css("margin-top"));
            var left = clientRect.left;
            var topRect = top * $("#feelingk_booktable").width();
            var rect = topRect + left;
            var percent = rect / totalRect * 100;
            return percent;
        } else {
        	 var totalRect = $(document).width() * $("#feelingk_booktable").height();
             var prePageRect = $(document).scrollLeft() * $("#feelingk_booktable").height();
             var top = clientRect.top - parseInt($("#feelingk_booktable").css("margin-top"));
             var left = clientRect.left;
             var topRect = top * $("#feelingk_booktable").width();
             var rect = prePageRect + topRect + left;
             var percent = rect / totalRect * 100;
             return percent;
        }
    }
    return -1;
}

function setMemoIcon() {

    $(".icon_memo").remove();

    var iconFilePath = window.selection.getMemoIconPath();

    var memos = $("." + MEMO_CLASS + "." + ANNOTATION_LASTSPAN_CLASS);

    var iconWidth = 10;
    var iconHeight = 10;

    for (var i=0; i<memos.length; ++i) {
    	var clientRects = memos[i].getClientRects();
    	var lastClientRect = $(clientRects).last()[0];
    	var firstClienRect = $(memos[i].getClientRects())[0];

    	var bottom = lastClientRect.height;

    	var imgTag = "<img class='icon_memo' src='" + iconFilePath + "'"+ " style='bottom:" + bottom +"px; width:" + iconWidth + "px; height:" + iconHeight + "px; max-width : "+iconWidth+"px;' />";
//    	+ " onclick='clickMemoIcon(" + $(memos[i]).attr('id') + ");' />";

    	$(memos[i]).append(imgTag);
    }
}

function isSearchHighlightInCurrentPage() {

    if ($("." + SEARCH_HIGHLIGHT).length <= 0) {
        return false;
    }
    var scrollTop = $('body').scrollTop();
    var scrollBottom = scrollTop + window.innerHeight;
    var searchHighlightTop = $("." + SEARCH_HIGHLIGHT).offset().top;

    if (searchHighlightTop >= scrollTop && searchHighlightTop <= scrollBottom) {
        return true;
    } else {
        return false;
    }
}

function log(text) {
    if(window.selection&&window.selection.print) {
        window.selection.print(text);
    }
}

/************************************************************************************ [s : new custom selection]  */
const MOVE_PREV = 1;                        // 핸들러 움직이는 방향
const MOVE_MIDDLE = 2;
const MOVE_NEXT = 3;

var textSelectionMode=false;                    // 셀렉션 모드 여부

var totalRange;                                 // 실제 저장 될 셀렉션 range
var totalRangeTemp;                             // 핸들러 움직임 체크를 위한 임시 셀렉션 range
var totalRangeContinuable;                      // 이어긋기 시 참조할 셀렉션 range

var startRange;                                 // 롱프레스 시 단어 판단을 위한 단어 range
var selectionStartCharacterRange;               // 실제 움직임을 판단하기 위한 첫 글자 range
var isSameStartCharacter=true;                  // 시작 글자에서 움직였는지 여부
var isStartWordOverNextPage=false;              // 첫 단어가 다음페이지 걸쳐 있는지 여부

var currentSelectionInfo;                       // 셀렉션 관련 정보

var currentSelectedHighlightId=null;            // 재활성화 아이디

var contextMenuTargetPosition = "END";          // 컨텍스트 메뉴 기준 핸들러 포지션

var notifyOverflowedTextSelection=false;        // 셀렉션 글자 수 제한 notify 여부
var notifyMergeOverflowedTextSelection=false;   // 셀렉션 병합 후 글자 수 제한 notify 여부
var notifyOverflowedTextSelectionAfterPaging=false;

var gMaxSelectionLength = 1000;                 // 셀렉션 글자 제한 기준 값

var isConfirmedOverflowCallback=false;
/***************************************************** s : new custom selection - make totalRange with user action */
function setStartSelectionRange(x,y) {

    console.log("SSIN setStartSelectionRange in");

//    if(textSelectionMode){
//        textSelectionMode = false;
//        window.selection.finishTextSelectionMode();
//        return;
//    }

    textSelectionMode = true;
    currentSelectedHighlightId = null;

    var currentElement = document.elementFromPoint(x, y);
    if(!checkSelectionAvailable(currentElement, null)){
         window.selection.finishTextSelectionMode();
//        textSelectionMode = false;
        return;
    }

    try {
        textSelectionMode = true;

        isSameStartCharacter = true;

        isStartWordOverNextPage = false;

        notifyOverflowedTextSelection = false;

        notifyMergeOverflowedTextSelection = false;

        totalRange=document.createRange();

        startRange = document.caretRangeFromPoint(x, y);

        selectionStartCharacterRange = startRange.cloneRange();

        var startContainer = startRange.startContainer;
        var startOffset = startRange.startOffset;
        var endOffset = startOffset;

        while (startOffset > 0) {
            startOffset -= 1;
            totalRange.setStart(startContainer, startOffset);
            if (/^\s|^\(|^\)/.test(totalRange.toString())) {
              startOffset += 1
              totalRange.setStart(startContainer, startOffset);
              startRange.setStart(startContainer, startOffset);
              break;
            }
        }

        var nodeLength = startContainer.textContent.length;
        while (endOffset <= nodeLength) {
            totalRange.setEnd(startContainer, endOffset);
            if (/\s$|\)$|\($/.test(totalRange.toString())) {
                totalRange.setEnd(startContainer, endOffset - 1);
                break;
            }
            endOffset += 1;
        }

        startRange.setEnd(startContainer, startOffset);

        currentSelectionInfo=new Object();
        currentSelectionInfo.isExistHandler = true;

        var rectList = getSelectedTextNodeRectList(totalRange);
        drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);

        if(gCurrentViewMode!=3){
           var selectedTextRects= totalRange.getClientRects();
           var lastRectRight = selectedTextRects[selectedTextRects.length-1].right;
           var checkValue = lastRectRight + $(document).scrollLeft();
           if(checkValue > $(document).scrollLeft() + gWindowInnerWidth){
                isStartWordOverNextPage = true;
           }
        }

    } catch(error){
        console.log("setStartSelectionRange error : "+error);
        window.selection.finishTextSelectionMode();
    }
}

function setMoveRange(x,y) {

    console.log("SSIN setMoveRange in");

    if(!textSelectionMode || currentSelectedHighlightId!=null) return;

    var currentElement = document.elementFromPoint(x, y);
    var moveRange = document.caretRangeFromPoint(x, y);

    var prevTotalRange = totalRange.cloneRange();

    if(!checkSelectionAvailable(currentElement, moveRange)){
        var rectList = getSelectedTextNodeRectList(totalRange);
        drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
        return;
    }

    try {
        if(isSameStartCharacter && selectionStartCharacterRange.startOffset==moveRange.startOffset && selectionStartCharacterRange.endOffset==moveRange.endOffset){
            // 시작 글자랑 같은 글자 머무른 경우 움직이지 않음으로 판단
            currentSelectionInfo.isExistHandler=true;
            var rectList = getSelectedTextNodeRectList(totalRange);
            drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
            return;
        } else {
            // 한번이라도 시작 글자랑 다르면 움직인 것으로 판단
            isSameStartCharacter=false;
        }

        var compare = startRange.compareBoundaryPoints(Range.START_TO_END, moveRange);   // 값은 startRange 기준으로 조건에 따라 나옴

        if(compare==0){
            return;
        }

        if(compare==1){
            totalRange.setStart(moveRange.startContainer, moveRange.startOffset);
            totalRange.setEnd(startRange.endContainer, startRange.endOffset);
        } else if(compare==-1){
            totalRange.setStart(startRange.startContainer, startRange.startOffset);
            totalRange.setEnd(moveRange.endContainer, moveRange.endOffset);
        }

        // totalRange.toString()이 공백을 포함하고 있으면 하이라이트 draw
        // totlaRange.toString()이 공백을 포함하고 있지 않으면 셀렉션 핸들 draw
        if ( /\s/g.test(totalRange.toString())) {
            currentSelectionInfo.isExistHandler = false;
        } else {
            currentSelectionInfo.isExistHandler = true;
        }

        if(gCurrentViewMode!=3 && !isStartWordOverNextPage){
            if(currentAndroidVersion.charAt(0)<=4){
                if(totalRange.endOffset>0){
                    var tempTotalRange = totalRange.cloneRange();
                    tempTotalRange.setEnd(tempTotalRange.endContainer, tempTotalRange.endOffset-1);
                    var selectedTextRects= tempTotalRange.getClientRects();
                    var lastRectRight = selectedTextRects[selectedTextRects.length-1].right;
                    var checkValue = lastRectRight + $(document).scrollLeft();
                    if(checkValue > $(document).scrollLeft() + gWindowInnerWidth){
                        totalRange = prevTotalRange.cloneRange();
                        return;
                    }
                }
            } else {
                var selectedTextRects= totalRange.getClientRects();
                var lastRectRight = selectedTextRects[selectedTextRects.length-1].right;
                var checkValue = lastRectRight + $(document).scrollLeft();
                if(checkValue > $(document).scrollLeft() + gWindowInnerWidth){
                    totalRange = prevTotalRange.cloneRange();
                    return;
                }
            }
        }

        var isOverflowTotalRange = checkSelectionMaxLength(prevTotalRange, 1);
        if(!isOverflowTotalRange){
            var rectList = getSelectedTextNodeRectList(totalRange);
            drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
        }

    } catch(error){
        console.log("setMoveRange error : "+error);
        window.selection.reportError(1);
    }
}

function setEndRange(x,y, colorIndex, selectionContinueCheck) {

    console.log("SSIN setEndRange in");

    if(!textSelectionMode) return;

    if(totalRange.toString().trim().length==0){
        textSelectionMode = false;
        window.selection.finishTextSelectionMode();
        return;
    }

    try {
        setSelectedText(totalRange.toString());

        totalRangeTemp = totalRange.cloneRange();

        var nextPageContinuable = checkNextPageContinuable(totalRange);

        if(!currentSelectionInfo.isExistHandler){
            // 롱프레스 시작 단어 밖에서 셀렉션 끝난 경우 - 퀵하이라이트
            // check. 퀵하이라이트 내 주석 포함 여부
            // - 포함 : 병합 시 메모 2000자 여부 체크
            // check. 다음페이지 이어긋기 대상 여부
            var isMergeMemoAvailable=true;
            if(isExistAnnotationInRange(totalRange)){
                isMergeMemoAvailable = window.selection.checkMemoMaxLength(JSON.stringify( getAnnotationIdList()));
                if(isMergeMemoAvailable){
                    if(nextPageContinuable){
                        showCurrentContextMenu(null, 4, contextMenuTargetPosition);
                    } else {
                        currentSelectionInfo=requestAnnotationInfo(totalRange, true);
                        highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
                        textSelectionMode = false;
                    }
                } else {
                    currentSelectionInfo.isExistHandler = true;
                    var rectList = getSelectedTextNodeRectList(totalRange);
                    drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
                    window.selection.overflowedMemoContent();
                    contextMenuTargetPosition = "END"
                    if(nextPageContinuable){
                        showCurrentContextMenu(null, 3, contextMenuTargetPosition);
                    } else {
                        showCurrentContextMenu(null, 2, contextMenuTargetPosition);
                    }
                }
            } else {
                if(nextPageContinuable){
                    if(!selectionContinueCheck){
                        showCurrentContextMenu(null, 4, contextMenuTargetPosition);
                    }
                } else {
                    currentSelectionInfo=requestAnnotationInfo(totalRange, true);
                    highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
                    textSelectionMode = false;
                }
            }
        } else {
            // 롱프레스 시작 단어 내에서 셀렉션 끝난 경우 - 셀렉션
            // check. 셀렉션 내 주석 포함 여부
            // check. 다음페이지 이어긋기 대상 여부
            // - 포함 : 수정 메뉴 (+페이지넘김)보여주기
            // - 미포함 : 신규 메뉴 (+페이지넘김)보여주기
            if(isExistAnnotationInRange(totalRange)){
                if(nextPageContinuable){
                    showCurrentContextMenu(null, 3, contextMenuTargetPosition);
                } else {
                    showCurrentContextMenu(null, 2, contextMenuTargetPosition);
                }
            } else {
                if(nextPageContinuable){
                    showCurrentContextMenu(null, 1, contextMenuTargetPosition);
                } else {
                    showCurrentContextMenu(null, 0, contextMenuTargetPosition);
                }
            }
        }
    } catch(error){
        console.log("setEndRange error : "+error);
        window.selection.reportError(1);
    }
}

function autoScroll(scrollToTop, scrollToBottom){

    if(notifyOverflowedTextSelection || notifyMergeOverflowedTextSelection){
        return;
    }

    if(scrollToTop){
         window.scrollBy(0, -20);
    }

    if(scrollToBottom){
         window.scrollBy(0, 20);
    }
}

function setMoveRangeWithHandler(x ,y, isStartHandlerTouched, isEndHandlerTouched){

    console.log("SSIN setMoveRangeWithHandler in");

    if(!textSelectionMode) {
        if(totalRange.toString().trim().length > 0){
            var rectList = getSelectedTextNodeRectList(totalRange);
            drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
        }
        return;
    }

    var currentElement = document.elementFromPoint(x, y);
    var moveRange = document.caretRangeFromPoint(x, y);

    var prevTotalRange = totalRange.cloneRange();

    if(!checkSelectionAvailable(currentElement, moveRange)){
        var rectList = getSelectedTextNodeRectList(totalRange);
        drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
        return;
    }

    try {
        var movePositionResult = positionCheck(x, y, moveRange);
        if(movePositionResult==null) {
            var rectList = getSelectedTextNodeRectList(totalRange);
            drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
            return;
        }
        var movingRange = movePositionResult.movingRange;
        if(movePositionResult.movePosition == MOVE_PREV) {
            if(isStartHandlerTouched) {
                totalRange.setStart(movingRange.startContainer, movingRange.startOffset);
                totalRange.setEnd(totalRangeTemp.endContainer, totalRangeTemp.endOffset);
            } else if(isEndHandlerTouched) {
                totalRange.setStart(movingRange.startContainer, movingRange.startOffset);
                totalRange.setEnd(totalRangeTemp.startContainer, totalRangeTemp.startOffset);
            }
        } else if(movePositionResult.movePosition == MOVE_NEXT) {
            if(isStartHandlerTouched) {
                totalRange.setStart(totalRangeTemp.endContainer, totalRangeTemp.endOffset);
                totalRange.setEnd(movingRange.endContainer, movingRange.endOffset);
            } else if(isEndHandlerTouched) {
                totalRange.setStart(totalRangeTemp.startContainer, totalRangeTemp.startOffset);
                totalRange.setEnd(movingRange.endContainer, movingRange.endOffset);
            }
        } else {
            if(isStartHandlerTouched) {
                contextMenuTargetPosition = "START";
                totalRange.setStart(movingRange.startContainer, movingRange.startOffset);
                totalRange.setEnd(totalRangeTemp.endContainer, totalRangeTemp.endOffset);
            } else if(isEndHandlerTouched) {
                contextMenuTargetPosition = "END";
                totalRange.setStart(totalRangeTemp.startContainer, totalRangeTemp.startOffset);
                totalRange.setEnd(movingRange.endContainer, movingRange.endOffset);
            }
        }

        if(gCurrentViewMode!=3 && !isStartWordOverNextPage){
            if(currentAndroidVersion.charAt(0)<=4){
                if(totalRange.endOffset>0){
                    var tempTotalRange = totalRange.cloneRange();
                    tempTotalRange.setEnd(tempTotalRange.endContainer, tempTotalRange.endOffset-1);
                    var selectedTextRects= tempTotalRange.getClientRects();
                    var lastRectRight = selectedTextRects[selectedTextRects.length-1].right;
                    var checkValue = lastRectRight + $(document).scrollLeft();
                    if(checkValue > $(document).scrollLeft() + gWindowInnerWidth){
                        totalRange = prevTotalRange.cloneRange();
                        return;
                    }
                }
            } else {
                var selectedTextRects= totalRange.getClientRects();
                var lastRectRight = selectedTextRects[selectedTextRects.length-1].right;
                var checkValue = lastRectRight + $(document).scrollLeft();
                if(checkValue > $(document).scrollLeft() + gWindowInnerWidth){
                    totalRange = prevTotalRange.cloneRange();
                    return;
                }
            }
        }

        var isOverflowTotalRange = checkSelectionMaxLength(prevTotalRange, 0);
        if(!isOverflowTotalRange){
            var rectList = getSelectedTextNodeRectList(totalRange);
            drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
        }
    } catch(error){
        console.log("setMoveRangeWithHandler error : "+error);
        window.selection.reportError(1);
    }
}

function positionCheck(x,y, movingRange) {

    var move_position = MOVE_PREV;

    var touchRangeStartElement = totalRangeTemp.startContainer;
    var touchRangeEndElement = totalRangeTemp.endContainer;

    if(movingRange != null) {
        var startCompare = movingRange.compareBoundaryPoints(Range.START_TO_START, totalRangeTemp);
        var endCompare = movingRange.compareBoundaryPoints (Range.END_TO_END, totalRangeTemp);

        if(startCompare == -1 && endCompare == -1) {
            move_position = MOVE_PREV;
            contextMenuTargetPosition = "START";
        } else if(startCompare == 1 && endCompare == 1) {
            move_position = MOVE_NEXT;
            contextMenuTargetPosition = "END";
        } else {
            move_position = MOVE_MIDDLE;
        }
        var result = new Object();
        result.movePosition = move_position;
        result.movingRange  = movingRange.cloneRange();
        return result;
    } else {
        return null;
    }
}

function setEndRangeWithHandler(x,y, colorIndex) {

    console.log("SSIN setEndRangeWithHandler in");

    if(!textSelectionMode)
        return;

    if(totalRange.toString().trim().length==0){
        textSelectionMode = false;
        window.selection.finishTextSelectionMode();
        return;
    }

    totalRangeTemp = totalRange.cloneRange();

    var nextPageContinuable = checkNextPageContinuable(totalRange);

    // 이동 완료 시
    // check. 셀렉션 내 주석 포함 여부
    // check. 다음페이지 이어 긋기 여부
    // - 포함 : 수정 메뉴 (+페이지넘김)보여주기
    // - 미포함 : 신규 메뉴 (+페이지넘김)보여주기
    if(isExistAnnotationInRange(totalRange)){
        if(nextPageContinuable && contextMenuTargetPosition == "END"){
            showCurrentContextMenu(null, 3, contextMenuTargetPosition);
        } else {
            showCurrentContextMenu(null, 2, contextMenuTargetPosition);
        }
    } else {
        if(nextPageContinuable && contextMenuTargetPosition == "END"){
            showCurrentContextMenu(null, 1, contextMenuTargetPosition);
        } else {
            showCurrentContextMenu(null, 0, contextMenuTargetPosition);
        }
    }
    setSelectedText(totalRange.toString());
}

function checkSelectionMaxLength(prevTotalRange, selectionType){

    var isOverflowAfterMoveRange = false;

    if(currentSelectedHighlightId!=null
        && totalRange.startContainer.parentElement.tagName.toLowerCase() == "flk"&& totalRange.startContainer.parentElement.title == currentSelectedHighlightId
        && totalRange.endContainer.parentElement.tagName.toLowerCase() == "flk"&& totalRange.endContainer.parentElement.title == currentSelectedHighlightId ){
        return false;
    } else {
        var prevOverflowedTextSelection = notifyOverflowedTextSelection;
        var prevMergeOverflowedTextSelection = notifyMergeOverflowedTextSelection;

        // #1. 실제 moveRange를 반영한 totalRange ( only 현재 셀렉션 된 영역 ) 글자 수 체크
        if(totalRange.startContainer.parentElement.tagName.toLowerCase() != 'flk' && totalRange.endContainer.parentElement.tagName.toLowerCase() != 'flk'){
            if(selectionType == 0 ){
                // 셀렉션
                if(totalRange.toString().length > gMaxSelectionLength){
                    if(prevTotalRange.toString().length > gMaxSelectionLength){                 // 페이지 넘김 후 움직이는 케이스
                        if(prevTotalRange.toString().length < totalRange.toString().length){    // 더 크게 범위 확장 불가
                            isOverflowAfterMoveRange = true;
                            notifyOverflowedTextSelection = true;
                            notifyMergeOverflowedTextSelection = false;
                        }
                    } else {
                        isOverflowAfterMoveRange = true;
                        notifyOverflowedTextSelection = true;
                    }
                }
            } else if(selectionType == 1 ){
                // 퀵
                if(totalRange.toString().length > gMaxSelectionLength){
                    isOverflowAfterMoveRange = true;
                    notifyOverflowedTextSelection = true;
                    notifyMergeOverflowedTextSelection = false;
                }
            }
        }

        // #2. 실제 moveRange를 반영한 totalRange가 기하이라이트에 걸친 경우 병합 후 글자 수 체크
        if(!isOverflowAfterMoveRange && totalRange.startContainer.parentElement.tagName.toLowerCase() == 'flk' && totalRange.endContainer.parentElement.tagName.toLowerCase() == 'flk'){

            var tempTotalRange = totalRange.cloneRange();

            var highlights = $("." + totalRange.startContainer.parentElement.title);
            var range = document.createRange();
            range.setStart(highlights[0].childNodes[0], 0);
            highlights =  $("." + totalRange.endContainer.parentElement.title);
            range.setEnd(highlights[highlights.length-1].childNodes[0], highlights[highlights.length-1].childNodes[0].textContent.length);

            if(tempTotalRange.toString().length > gMaxSelectionLength) {
                isOverflowAfterMoveRange = true;
                notifyOverflowedTextSelection = false;
                notifyMergeOverflowedTextSelection = true;
            }
        }

        var tempTotalRange = totalRange.cloneRange();
        if(!isOverflowAfterMoveRange && isExistAnnotationInRange(tempTotalRange)){
            var containTarget = getAnnotationIdList();
            for(var idx=0; idx<containTarget.length; idx++){
                var highlights = $("." + containTarget[idx]);
                var range = document.createRange();
                range.setStart(highlights[0].childNodes[0], 0);
                range.setEnd(highlights[highlights.length-1].childNodes[0], highlights[highlights.length-1].childNodes[0].textContent.length);

                var compareStart = range.compareBoundaryPoints(Range.START_TO_START, tempTotalRange);
                var compareEnd = range.compareBoundaryPoints(Range.END_TO_END, tempTotalRange);
                var compareStartEnd = range.compareBoundaryPoints(Range.START_TO_END, tempTotalRange);
                var compareEndStart = range.compareBoundaryPoints(Range.END_TO_START, tempTotalRange);

                if(compareStart == 1 && compareEnd == 1 && compareEndStart!=0) {
                    tempTotalRange.setEnd(range.endContainer, range.endOffset);
                } else if(compareStart == -1 && compareEnd == -1 && compareStartEnd!=0) {
                    tempTotalRange.setStart(range.startContainer, range.startOffset);
                }
            }

            if(tempTotalRange.toString().length > gMaxSelectionLength) {
                isOverflowAfterMoveRange = true;
                notifyOverflowedTextSelection = false;
                notifyMergeOverflowedTextSelection = true;
            }
        }

        if(isOverflowAfterMoveRange){
            totalRange = prevTotalRange.cloneRange();
            notifyOverflowToFront();
            return true;
        } else {
            isConfirmedOverflowCallback = false;
            notifyOverflowedTextSelection = false;
            notifyMergeOverflowedTextSelection = false;
            return false;
        }
    }
}

function notifyOverflowToFront(){
    if(!isConfirmedOverflowCallback){
        isConfirmedOverflowCallback = true;
        if(notifyOverflowedTextSelection){
             window.selection.overflowedTextSelection(0);
        } else if(notifyMergeOverflowedTextSelection){
             window.selection.overflowedTextSelection(1);
        }
    }
}

function showCurrentContextMenu(highlightID, menuTypeIndex, contextMenuPosition){
    var currentSelectedRect = totalRange.getClientRects();
    var startRect = currentSelectedRect[0];
    var endRect = currentSelectedRect[currentSelectedRect.length-1];
    window.selection.showContextMenu( highlightID, menuTypeIndex, contextMenuPosition, endRect.right, endRect.top, endRect.bottom, startRect.left, startRect.top, startRect.bottom);
}

function showCurrentHighlightSelection(highlightID){

    console.log("showCurrentHighlightSelection highlightID : "+highlightID);

    isConfirmedOverflowCallback = false;

    var annotationElms = document.getElementsByClassName(highlightID);

    if(annotationElms.length == 0)
        return;

    try{
        textSelectionMode = true;

        currentSelectedHighlightId = highlightID;

        var tempRange = document.createRange();
        tempRange.setStart(annotationElms[0], 0);
        var endElement = annotationElms[annotationElms.length-1];
        tempRange.setEnd(endElement, endElement.childNodes.length);

        var nodesToSelected=new Array();
        findAllFullTextNodesInRange(nodesToSelected, tempRange);

        totalRange = document.createRange();
        totalRange.setStart(nodesToSelected[0], 0);
        totalRange.setEnd(nodesToSelected[nodesToSelected.length-1],nodesToSelected[nodesToSelected.length-1].textContent.length);

        currentSelectionInfo=requestAnnotationInfo(totalRange, true);

        var touchRectList = getSelectedTextNodeRectList(totalRange);

        // rect 중복 제거
        for(var i=0; i<touchRectList.length; i++) {
            var j=i+1;
            while(j<touchRectList.length) {
                if(touchRectList[i].left == touchRectList[j].left && touchRectList[i].top == touchRectList[j].top) {
                    touchRectList.splice(j, 1);
                } else {
                    j++;
                }
            }
        }

        drawSelectionRect(touchRectList, currentSelectionInfo.isExistHandler);

        totalRangeTemp = totalRange.cloneRange();

        setSelectedText(totalRange.toString());

        if(gCurrentViewMode!=3){
           var selectedTextRects= totalRange.getClientRects();
           var lastRectRight = selectedTextRects[selectedTextRects.length-1].right;
           var checkValue = lastRectRight + $(document).scrollLeft();
           if(checkValue > $(document).scrollLeft() + gWindowInnerWidth){
                isStartWordOverNextPage = true;
           }
        }

        setTimeout(function () { showCurrentContextMenu(highlightID, 2, contextMenuTargetPosition);}, 100);

    } catch(error){
        console.log("showCurrentHighlightSelection error : "+error+ " / highlightID : "+ highlightID);
    }
}
/***************************************************** e : new custom selection - make totalRange with user action */

function getStartElementInfoFromSelection(range) {

    var startContainerElement = getAncestorElementForTextNode(range.startContainer);
    var startOffset = range.startOffset;

    if (range.startContainer.nodeType != ELEMENT_NODE){
        var node = range.startContainer.previousSibling;
        while (node) {
            startOffset += node.textContent.length;
            node = node.previousSibling;
        }
    }

    // 주석, 검색 하이라이트에 걸쳤을때
    // 주석, 검색 하이라이트 상위 엘리먼트를 구한다.
    var originalStartElement = startContainerElement;
    while (startContainerElement.className.indexOf(HIGHLIGHT_CLASS) != -1 ||
            startContainerElement.className.indexOf(SEARCH_HIGHLIGHT) != -1 &&
           startContainerElement.parentElement.id != "feelingk_bookcontent") {
        startContainerElement = startContainerElement.parentElement;
    }

    while (!(/(div|section|figure)/i.test(startContainerElement.parentElement.tagName.toLowerCase())) &&
           startContainerElement.id != "feelingk_bookcontent") {
        startContainerElement = startContainerElement.parentElement;
    }

    var startElementPath = $(startContainerElement).getPath();

    // startOffset을 다시 구한다.
    while (originalStartElement.id != "feelingk_bookcontent") {
        if (originalStartElement == startContainerElement)
            break;

        var parentEl = originalStartElement.parentElement;
        var childNodes = parentEl.childNodes;
        for (var i = 0; i < childNodes.length; ++i) {
            if (originalStartElement == childNodes[i])
                break;
            startOffset += childNodes[i].textContent.length;
        }
        originalStartElement = parentEl;
    }

    var result = new Object();
    result.path = startElementPath;
    result.offset = startOffset;
    result.text = $(startElementPath)[0].textContent.charAt(result.offset);
    return result;
}

function getEndElementInfoFromSelection(range) {

    var endContainerElement = getAncestorElementForTextNode(range.endContainer);
    var endOffset = range.endOffset;

    if (range.endContainer.nodeType != ELEMENT_NODE) {
        var node = range.endContainer.previousSibling;
        while (node) {
            endOffset += node.textContent.length;
            node = node.previousSibling;
        }
    }

    // 주석, 검색 하이라이트에 걸쳤을때
    // 주석, 검색 하이라이트 상위 엘리먼트를 구한다.
    var originalStartElement = endContainerElement;
    while (endContainerElement.className.indexOf(HIGHLIGHT_CLASS) != -1 ||
            endContainerElement.className.indexOf(SEARCH_HIGHLIGHT) != -1 &&
           endContainerElement.parentElement.id != "feelingk_bookcontent") {
        endContainerElement = endContainerElement.parentElement;
    }

    while (!(/(div|section|figure)/i.test(endContainerElement.parentElement.tagName.toLowerCase())) &&
           endContainerElement.id != "feelingk_bookcontent") {
        endContainerElement = endContainerElement.parentElement;
    }

    var endElementPath =  $(endContainerElement).getPath();

    // endOffset을 다시 구한다.
    while (originalStartElement.id != "feelingk_bookcontent") {
        if (originalStartElement == endContainerElement)
            break;

        var parentEl = originalStartElement.parentElement;
        var childNodes = parentEl.childNodes;
        for (var i = 0; i < childNodes.length; ++i) {
            if (originalStartElement == childNodes[i])
                break;
            endOffset += childNodes[i].textContent.length;
        }
        originalStartElement = parentEl;
    }

    var result = new Object();
    result.path = endElementPath;
    result.offset = endOffset;
    result.text = $(endElementPath)[0].textContent.charAt(result.offset);
    return result;
  }

function getSelectedTextNodeRectList(totalRange) {

    var textNodeRectList = [];

    if (totalRange.startContainer === totalRange.endContainer) {
        var rects = totalRange.getClientRects();
        for (var i = 0; i < rects.length; i += 1)
            textNodeRectList.push(rects[i]);
        return textNodeRectList;
    }

    var nodeIterator = document.createNodeIterator(
        totalRange.commonAncestorContainer,
        NodeFilter.SHOW_TEXT,
        { acceptNode: function(node) {
                if ( ! /^\s*$/.test(node.data) ) {
                    return NodeFilter.FILTER_ACCEPT;
                }
            }
        },false);

    var tempRange=document.createRange();
    tempRange.setStart(totalRange.startContainer, totalRange.startOffset);
    tempRange.setEnd(totalRange.startContainer, totalRange.startContainer.textContent.length);

    var rects = tempRange.getClientRects();
    for (var i = 0; i < rects.length; i += 1)
        textNodeRectList.push(rects[i]);

    var node;
    while ((node = nodeIterator.nextNode())) {
        if (totalRange.startContainer.compareDocumentPosition(node) === Node.DOCUMENT_POSITION_FOLLOWING && totalRange.startContainer !== node) {
            if (totalRange.endContainer.compareDocumentPosition(node) === Node.DOCUMENT_POSITION_FOLLOWING || totalRange.endContainer === node)
                break;
            tempRange = document.createRange();
            tempRange.selectNodeContents(node);
            var rects = tempRange.getClientRects();
            for (var i = 0; i < rects.length; i += 1)
                textNodeRectList.push(rects[i]);
        }
    }

    tempRange = document.createRange();
    tempRange.setStart(totalRange.endContainer, 0);
    tempRange.setEnd(totalRange.endContainer, totalRange.endOffset);
    var rects = tempRange.getClientRects();
    for (var i = 0; i < rects.length; i += 1)
        textNodeRectList.push(rects[i]);

    return textNodeRectList;
}

function drawSelectionRect(rectList, isExistHandler){
    var rects = new Array();
    for(var i=0; i<rectList.length; i++){
        var rect = getSelectionRectObject(rectList[i].left, rectList[i].top,  rectList[i].right, rectList[i].bottom, rectList[i].width, rectList[i].height);
        if (rect != null)
            rects.push(rect);
    }
    window.selection.drawSelectionRect(JSON.stringify(rects), isExistHandler);
}

function checkNextPageContinuable(range) {

    var canSelectContinuously = false;
    if (gCurrentViewMode!=3) {
//        if(totalRange.endContainer.nodeType!=3)
//            console.log("SSIN checkNextPageContinuable : "+totalRange.endContainer.nodeName);
        return isExistMoreText(range.endContainer, range.endOffset);
    }
    return canSelectContinuously;
}

function isExistMoreText(textNode, currentEndOffset){

    var maxRight = gWindowInnerWidth;
    var canSelectContinuously = false;
    var tempRange = totalRange.cloneRange();

    if(textNode==null || textNode.length==0)
        return canSelectContinuously;

    for(var endOffset=currentEndOffset; endOffset<textNode.textContent.length; endOffset+=1){

        tempRange.setStart(textNode, endOffset);
        tempRange.setEnd(textNode, endOffset+1);

        if(/\s/.test(tempRange.toString()))
            continue;

        if(tempRange.getBoundingClientRect().left+tempRange.getBoundingClientRect().width>maxRight){
            canSelectContinuously = true;
//            totalRange.setEnd(tempRange.endContainer, tempRange.endOffset-1);
            totalRangeContinuable = totalRange.cloneRange();
            totalRangeContinuable.setEnd(tempRange.endContainer, tempRange.endOffset-1);
            return canSelectContinuously;
        }else {
            return canSelectContinuously;
        }
    }
    return isExistMoreText(getNextTextNode(tempRange.endContainer),0);
}

function getNextTextNode(currentSelectedEndContainer) {

    var nextSiblingNode = currentSelectedEndContainer.nextSibling;

    if(nextSiblingNode!=null){
        // 현재 노드 기준 같은 부모 내 다음 자식 노드 존재 시
        if(nextSiblingNode.nodeType==3 ){
            // 현재 노드 기준 같은 부모 내 다음 자식 노드가 텍스트 노드인 경우
            if(nextSiblingNode.textContent.trim().length==0){
                return getNextTextNode(nextSiblingNode);
            } else {
                return nextSiblingNode;
            }
        } else{
            // 현재 노드 기준 같은 부모 내 다음 자식 노드가 엘리먼트 노드인 경우
            var nodeIterator = document.createNodeIterator(
                    nextSiblingNode,
                    NodeFilter.SHOW_TEXT,
                    { acceptNode: function(node) {
                            if ( ! /^\s*$/.test(node.data) ) {
                                return NodeFilter.FILTER_ACCEPT;
                            }
                        }
                    },
                    false
                );
             var textNodeInNextSiblingNode = nodeIterator.nextNode();
             if(textNodeInNextSiblingNode!=null){
                // 엘리먼트 내 텍스트 노드 있는 경우
                return textNodeInNextSiblingNode;
             } else {
                return getNextTextNode(nextSiblingNode);
             }
        }
    } else {
        // 현재 노드 기준 부모의 자식 노드가 더이상 존재하지 않는 경우
        if(currentSelectedEndContainer.parentNode.id =='feelingk_bookcontent' || currentSelectedEndContainer.parentNode.id =='feelingk_booktable' || currentSelectedEndContainer.parentNode.id =='feelingk_modify_bookcontent' ){
            return null;
        }
        return getNextTextNode(currentSelectedEndContainer.parentNode);
    }
}

function findEndRangeAndOffset(targetPageRight, textNode, currentEndOffset){

    var findEndPosition = false;

    var currentLeft = $(document).scrollLeft();

    if(textNode==null)
        return findEndPosition = false;

    var tempRange = totalRangeContinuable.cloneRange();

    for(var endOffset=currentEndOffset; endOffset<textNode.textContent.length; endOffset+=1){
        tempRange.setStart(textNode, endOffset);
        tempRange.setEnd(textNode, endOffset+1);

        if(tempRange.startContainer.parentElement.tagName.toLowerCase()!='flk'){
            totalRange.setEnd(tempRange.endContainer, tempRange.endOffset);
        }

        if (/\.$|\!$|\?$/.test(tempRange.toString())){
            if(currentLeft+tempRange.getBoundingClientRect().left+tempRange.getBoundingClientRect().width<targetPageRight){      // 마침조건 O
                totalRangeContinuable.setEnd(tempRange.endContainer, tempRange.endOffset);
                return findEndPosition = true;
            } else {
                return findEndPosition = false;
            }
        }

        if(currentLeft+tempRange.getBoundingClientRect().left+tempRange.getBoundingClientRect().width>targetPageRight){      // 마침조건 X
            return findEndPosition = false;
        }
    }
    return findEndRangeAndOffset(targetPageRight, getNextTextNode(tempRange.endContainer), 0);
}

function getSelectionLandingPage(isHighlight, highlightColorIndex){

    var lastRangeInCurrentPage = totalRange.cloneRange();

    var tempRange = totalRangeContinuable.cloneRange();
    if(tempRange.endOffset<tempRange.endContainer.textContent.length){
        tempRange.setEnd(tempRange.endContainer, tempRange.endOffset+1);
    }

    var currentLeft = $(document).scrollLeft();
    var targetPageIdx = gCurrentPage;
    var targetPageLeft = currentLeft;
    var checkValue = tempRange.getBoundingClientRect().right+currentLeft;
    while(targetPageLeft<checkValue){
        if(targetPageLeft+gWindowInnerWidth<checkValue){
            targetPageIdx+=1;
            targetPageLeft += gWindowInnerWidth;
        } else {
            break;
        }
    }

    var findEndPosition = findEndRangeAndOffset(targetPageLeft+gWindowInnerWidth, tempRange.endContainer, tempRange.endOffset);
    if(!findEndPosition){
        var tempRange = totalRangeContinuable.cloneRange();
        while(tempRange.endOffset<tempRange.endContainer.textContent.length){
            tempRange.setEnd(tempRange.endContainer, tempRange.endOffset+1);
            if (/\s$/.test(tempRange.toString())) {
                totalRangeContinuable = tempRange.cloneRange();
                break;
            }
			 totalRangeContinuable = tempRange.cloneRange();
        }
    }

    var mergeCheckRange = totalRangeContinuable.cloneRange();

    if(totalRangeContinuable.startContainer.parentElement.tagName.toLowerCase()=='flk'){
        var highlights = $("." + totalRangeContinuable.startContainer.parentElement.title);
        if(totalRangeContinuable.startOffset !== highlights[highlights.length-1].textContent.length){
            mergeCheckRange.setStart(highlights[0].childNodes[0], 0);
        }
    }

    if(totalRangeContinuable.endContainer.parentElement.tagName.toLowerCase()=='flk'){
        var highlights = $("." + totalRangeContinuable.endContainer.parentElement.title);
        mergeCheckRange.setEnd(highlights[highlights.length-1].childNodes[0], highlights[highlights.length-1].childNodes[0].textContent.length);
    }

    notifyOverflowedTextSelectionAfterPaging=false;
    if(mergeCheckRange.toString().length>gMaxSelectionLength){
        notifyOverflowedTextSelectionAfterPaging = true;
        mergeCheckRange.setEnd(totalRange.endContainer, totalRange.endOffset);
        if(mergeCheckRange.toString().length>gMaxSelectionLength){
            targetPageIdx = gCurrentPage;
            notifyOverflowedTextSelectionAfterPaging = false;
        } else {
            targetPageIdx = gCurrentPage;
            targetPageLeft = currentLeft;
            var checkValue = totalRange.getBoundingClientRect().right+targetPageLeft;
            while(targetPageLeft<checkValue){
                if(targetPageLeft+gWindowInnerWidth<checkValue){
                    targetPageIdx+=1;
                    targetPageLeft += gWindowInnerWidth;
                } else {
                    break;
                }
            }
        }
    } else {
        totalRange=totalRangeContinuable.cloneRange();
    }

    if(targetPageIdx == gCurrentPage){
        notifyOverflowedTextSelectionAfterPaging = false;
        totalRange = lastRangeInCurrentPage.cloneRange();
        contextMenuTargetPosition = "END";
        window.selection.overflowedTextSelection(2);
        if(isHighlight){
            addAnnotation(highlightColorIndex);
        } else {
            if(isExistAnnotationInRange(totalRange)){
                showCurrentContextMenu(null, 2, contextMenuTargetPosition);
            } else {
                showCurrentContextMenu(null, 0, contextMenuTargetPosition);
            }
        }
    }

    if(/\s$/.test(totalRange.toString())){   // endsWith() 36부터 지원해서 못씀
        totalRange.setEnd(totalRange.endContainer, totalRange.endOffset-1);
    }
    window.selection.setLandingPage(targetPageIdx);
}

function selectionContinue(isHighlight, colorIndex){

    if(isHighlight){
        var isMergeMemoAvailable=true;
        if(isExistAnnotationInRange(totalRange)){
            isMergeMemoAvailable = window.selection.checkMemoMaxLength(JSON.stringify( getAnnotationIdList()));
            if(isMergeMemoAvailable){
                currentSelectionInfo=requestAnnotationInfo(totalRange, false);
                highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
                textSelectionMode = false;
            } else {
                currentSelectionInfo=requestAnnotationInfo(totalRange, true);
                var rectList = getSelectedTextNodeRectList(totalRange);
                drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
                window.selection.overflowedMemoContent();
                contextMenuTargetPosition = "END"
                window.selection.invalidateSelectionDraw();
                setTimeout(function () { showCurrentContextMenu(null, 2, contextMenuTargetPosition);}, 100);
            }
        } else {
            var currentSelectionInfo=requestAnnotationInfo(totalRange, false);
            highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
            textSelectionMode=false;
        }
    } else {
        contextMenuTargetPosition = "END";

        var menuTypeIndex = -1;
        if(isExistAnnotationInRange(totalRange)){
            menuTypeIndex = 2;
        } else {
            menuTypeIndex = 0;
        }

        var rectList = getSelectedTextNodeRectList(totalRange);
        drawSelectionRect(rectList, true);

        textSelectionMode=true;

        setTimeout(function () { showCurrentContextMenu(null, menuTypeIndex, contextMenuTargetPosition);}, 100);
    }

    if(notifyOverflowedTextSelectionAfterPaging){
        window.selection.overflowedTextSelection(1);
        notifyOverflowedTextSelectionAfterPaging=false;
    }
}

function requestAllMemoText(){
    var containTarget = getAnnotationIdList();
    window.selection.mergeAllMemoText(JSON.stringify(containTarget));
}

function getAnnotationIdList(){

    var containTarget = [];
    var flkTags = document.getElementsByTagName('flk');
    for(var i=0; i<flkTags.length; i++){
        if(totalRange.intersectsNode(flkTags[i])){
            if($.inArray(flkTags[i].title, containTarget) === -1)
                containTarget.push(flkTags[i].title);
        }
    }
    return containTarget;
}

function highlightFromSelection(startElementPath, endElementPath, startCharOffset, endCharOffset, clrIndex){
    highlightFromSelectionWithMemo(startElementPath, endElementPath, startCharOffset, endCharOffset, clrIndex, "");
}

function highlightFromSelectionWithMemo(startElementPath, endElementPath, startCharOffset, endCharOffset, clrIndex, memoContent) {

	startElementPath = stringDotRevision(startElementPath);
	endElementPath = stringDotRevision(endElementPath);

    try {
        var snippetStartNodeElement=getNodeFromElementPath(startElementPath.replace(/epub:/gi, 'epub\\:'), 0);
        var snippetEndNodeElement=getNodeFromElementPath(endElementPath.replace(/epub:/gi, 'epub\\:'), 0);
        var startElement = getAncestorElementForTextNode(snippetStartNodeElement);
        var endElement = getAncestorElementForTextNode(snippetEndNodeElement);

        if(snippetStartNodeElement===null) {
            throw("No snippet start element");

        } else if(startCharOffset>=0) {

            if( checkContainSVG(totalRange) ){
            	throw("selction range has svg tag");
            }

            var rootElementNode = $('#feelingk_bookcontent')[0];
		    var startTopChildNode = getNodeWithoutHighlight(startElement);
		    var endTopChildNode = getNodeWithoutHighlight(endElement);
		    var startRootChildNode = getRootChildNode(startTopChildNode, rootElementNode);
		    var endRootChildNode = getRootChildNode(endTopChildNode, rootElementNode);

            // 최상단 엘리먼트 경로
            var startTopElementPath = $(startRootChildNode).getPath();
            var endTopElementPath = $(endRootChildNode).getPath();

            // 이전 노드의 Char Offset
            var startNode = $(startTopElementPath)[0]; // startContainerElement
            var prevStartCharOffset = prevNodeCharOffset(startNode, snippetStartNodeElement);

            var endNode = $(endTopElementPath)[0]; // endContainerElement
            var prevEndCharOffset = prevNodeCharOffset(endNode, snippetEndNodeElement);

            startCharOffset = startCharOffset+prevStartCharOffset;
            endCharOffset = endCharOffset+prevEndCharOffset;

            var root = $('#feelingk_bookcontent').children();
            var startChildIndex = getChildIndex(root, startTopElementPath, rootElementNode);
            var endChildIndex = getChildIndex(root, endTopElementPath, rootElementNode);

            var highlightToSave = getHighlightData(generateUniqueClass(), startTopElementPath, endTopElementPath, startChildIndex, endChildIndex, startCharOffset, endCharOffset, clrIndex, memoContent);

            highlightToSave.isMemo = false;
            if(memoContent.length>0)
                highlightToSave.isMemo = true;

        	window.selection.checkMergeAnnotation( JSON.stringify(highlightToSave) );
        } else {
            throw("Snippet start offset is negative");
        }
    } catch(err) {
        console.log("error highlighting selection 2: "+err);
        window.selection.reportError(5);
        window.selection.finishedApplyingHighlight(false);
    }
}

function highlightText(uniqueId, startElementPath, endElementPath, startCharOffset, endCharOffset, highlightID, clrIndex, Ids, memoText) {

	startElementPath = stringDotRevision(startElementPath);
	endElementPath = stringDotRevision(endElementPath);

	var saved=false;

	try {
        var snippetStartNodeElement=$(startElementPath)[0];
        var snippetEndNodeElement=$(endElementPath)[0];

        if(snippetStartNodeElement===null) {
            log("No snippet start element");
        } else if(startCharOffset>=0) {

            var range=document.createRange();

            if(!setPointInRange(snippetStartNodeElement, startCharOffset, range, setRangeStart)) {
                throw("Could not set start of selection range");
            }

            if(!setPointInRange(snippetEndNodeElement, endCharOffset, range, setRangeEnd)) {
                throw("Could not set end of selection range");
            }

            var rootElementNode = $('#feelingk_bookcontent')[0];
            var root = $('#feelingk_bookcontent').children();
            var startChildIndex = getChildIndex(root, startElementPath, rootElementNode);
            var endChildIndex = getChildIndex(root, endElementPath, rootElementNode);

		    var highlightToSave=getHighlightData(highlightID, startElementPath, endElementPath, startChildIndex, endChildIndex, startCharOffset, endCharOffset,clrIndex);

		    var selectedText = range.toString();

		    var spans=highlightRange(range,false,true,highlightID,clrIndex);

		    highlightToSave.percent = getPercentOfRange(range);

		    range.detach();

            var spanId = "";
            var lastId = '';
            highlightToSave.uniqueId = uniqueId;
            highlightToSave.spanId = spanId;
            highlightToSave.chapterId = lastId;
            highlightToSave.colorIndex = clrIndex;
            highlightToSave.text = selectedText;
            highlightToSave.memo = memoText;
            saved=saveHighlight(highlightToSave);

            if(memoText.length>0){
                $("[title=\""+highlightID+"\"]").addClass(MEMO_CLASS);
                setMemoIcon();
            }
        }
    } catch(err) {
        console.log("error highlighting selection 1: "+err);
        window.selection.reportError(5);
        window.selection.finishedApplyingHighlight(saved);
    }
  }

function addAnnotation(colorIndex){

    if(totalRange == undefined || totalRange == null || totalRange.toString().length==0)
        return;

    var isMergeMemoAvailable=true;
    if(isExistAnnotationInRange(totalRange)){
        isMergeMemoAvailable = window.selection.checkMemoMaxLength(JSON.stringify( getAnnotationIdList()));
        if(isMergeMemoAvailable){
            var currentSelectionInfo=requestAnnotationInfo(totalRange, false);
            highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
            textSelectionMode = false;
        } else {
            window.selection.overflowedMemoContent();
        }
    } else {
        var currentSelectionInfo=requestAnnotationInfo(totalRange, false);
        highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
        textSelectionMode = false;
    }
}

function addAnnotationWithMemo(colorIndex, memoContent){

    if(totalRange == undefined || totalRange == null || totalRange.toString().length==0)
        return;

    var currentSelectionInfo=requestAnnotationInfo(totalRange, false);
    highlightFromSelectionWithMemo(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex, memoContent[0]);
    textSelectionMode = false;
}

function modifyAnnotationColorAndRange(colorIndex){

    if(totalRange == undefined || totalRange == null || totalRange.toString().length==0)
        return;

    var isMergeMemoAvailable=true;
    if(isExistAnnotationInRange(totalRange)){
         isMergeMemoAvailable = window.selection.checkMemoMaxLength(JSON.stringify( getAnnotationIdList()));
        if(isMergeMemoAvailable){
            var currentSelectionInfo = requestAnnotationInfo(totalRange, false);
            highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
            textSelectionMode = false;
        } else {
            window.selection.overflowedMemoContent();
        }
    } else {
        var currentSelectionInfo = requestAnnotationInfo(totalRange, false);
        highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
        textSelectionMode = false;
    }
}

function deleteAnnotationInRange(){

    textSelectionMode =false;

    var deleteTarget = [];
    var parentTarget = [];
    var flkTags = document.getElementsByTagName('flk');
    for(var i=0; i<flkTags.length; i++){
        if(totalRange.intersectsNode(flkTags[i])){
            if($.inArray(flkTags[i].title, deleteTarget) === -1)
                deleteTarget.push(flkTags[i].title);
        }
    }

    for(var i=0; i<deleteTarget.length; i++){
        var highlightSpans=$("[title=\"" + deleteTarget[i] + "\"]");
        for(var j=0; j<highlightSpans.length; j++){
            var parentElement = $(highlightSpans[j])[0].parentElement;
            if($.inArray(parentElement, parentTarget) === -1)
                parentTarget.push(parentElement);
        }
    }

    for(var i=0; i<deleteTarget.length; i++){
        var highlightSpans=$("[title=\"" + deleteTarget[i] + "\"]");
        if( highlightSpans != null ) {
            $(highlightSpans).contents().unwrap();
        }
    }

    for(var i=0; i<parentTarget.length; i++){
        parentTarget[0].normalize();
    }

    window.selection.addDeleteHistory(JSON.stringify(deleteTarget));

    setMemoIcon();
}

function checkSelectionAvailable(element, checkRange){
    if(element.tagName.toUpperCase()=="HTML" || element.tagName.toUpperCase()=='BODY' || element.tagName.toUpperCase()=="IMG" || element.tagName.toUpperCase()=="AUDIO" || element.tagName.toUpperCase()=="VIDEO" ||
        $(element).getPath().toLowerCase().indexOf('>svg') != -1 ) { // || element.id == "feelingk_booktable" || element.id == "feelingk_bookcontent" || element.id == "feelingk_modify_bookcontent"
            return false;
    } else if(checkRange!=null && checkRange.startContainer.nodeType!=TEXT_NODE){
        return false;
    }
    return true;
}

function checkInnerSelection(currentRange){
    var startElementParent = currentRange.startContainer.parentElement;
    var endElementParent = currentRange.endContainer.parentElement;
    var startElementTag = startElementParent.tagName.toLowerCase();
    var endElementTag = endElementParent.tagName.toLowerCase();
    var startElementTitle = startElementParent.title;
    var endElementTitle = endElementParent.title;
    if(startElementTag=='flk' && endElementTag=='flk' && startElementTitle==endElementTitle){
        return true;
    } else {
        return false;
    }
}

function isExistAnnotationInRange(range) {

    var hasAnnotation=false;

    if(checkInnerSelection(totalRange)){    // 같은 엘리먼트 안 이너셀렉션은 아래 querySelectorAll 결과 null
        hasAnnotation = true;
    } else {
        var nodes = range.cloneContents().querySelectorAll('.'+HIGHLIGHT_CLASS);
        for(var i=0;i<nodes.length;i++){
            if(nodes[i].textContent.length>0){
                hasAnnotation = true;
                break;
            }
        }
    }
    return hasAnnotation;
}

function requestAnnotationInfo(range, isExistHandler){
    var startElementInfo = getStartElementInfoFromSelection(range);
    var endElementInfo = getEndElementInfoFromSelection(range);
    currentSelectionInfo=new Object();
    currentSelectionInfo.startElementPath=startElementInfo.path;
    currentSelectionInfo.startCharOffset=startElementInfo.offset;
    currentSelectionInfo.endElementPath=endElementInfo.path;
    currentSelectionInfo.endCharOffset= endElementInfo.offset;
    currentSelectionInfo.isExistHandler = isExistHandler;
    return currentSelectionInfo;
}

function setSelectedText(selectedText){
    window.selection.setSelectedText(selectedText);
}

function finishTextSelection(){
    textSelectionMode=false;
    selectionScrolling=false;
    isConfirmedOverflowCallback=false;
    notifyOverflowedTextSelection=false;
    notifyMergeOverflowedTextSelection=false;
    notifyOverflowedTextSelectionAfterPaging=false;
    contextMenuTargetPosition="END";
}
/********************************************************* e : selection common function test  */

/********************************************************* s : kindle way selection continue test */
//function doSelectionContinue(){
//
//    /********************* s : kindle way selection continue test */
//    var nodesOnCurrentPage=[];
//
//    var allNodes = $("#feelingk_bookcontent *").map(function(index) { return this;});
//
//    var viewportWidth = $(window).width();
//    var scrLeft=$(document).scrollLeft();
//    var scrRight=scrLeft + viewportWidth;
//    var textNodesOnCurrentPage=[];
//    var lastElement;
//    for (var i=0; i<allNodes.length; i++) {
//        if(allNodes[i].id=="feelingk_modify_bookcontent" || getElementDefaultDisplay(allNodes[i].tagName) !='block')
//            continue;
//        var leftOffset = $(allNodes[i]).offset().left;
//        if (leftOffset >= scrLeft && leftOffset < scrRight){
//            lastElement = allNodes[i];
//        }
//    }
//
//    nativeTreeWalker(lastElement, textNodesOnCurrentPage);
//
//    // TODO :: nativeTreeWalker 가 없으면 다음 페이지로 이동
//
//    var rect;
//	var rectList = new Array();
//	for (var i=0; i<textNodesOnCurrentPage.length; i++) {
//        var tempRange = document.createRange();
//        tempRange.selectNode(textNodesOnCurrentPage[i]);
//        var clientRects = tempRange.getClientRects();
//        for(var j=0; j<clientRects.length; j++){
//            rectList.push(clientRects[j]);
//        }
//	}
//
//    var lastTextData = getLastTextWithinRange(rectList);
//    var moveRange = document.caretRangeFromPoint(lastTextData.right, lastTextData.bottom);
//    totalRange.setEnd(moveRange.endContainer, moveRange.endOffset);
//    var currentRects = getSelectedTextNodeRectList(totalRange);
//	console.log("SSIN totalRange continue : "+totalRange.toString());
//	currentSelectionInfo.isExistHandler=true;
//    drawSelectionRect(currentRects, currentSelectionInfo.isExistHandler);
//
////    //for check
////    var startElementInfo = getStartElementInfoFromSelection(totalRange);
////    currentSelectionInfo.startElementPath=startElementInfo.path;
////    currentSelectionInfo.startCharOffset=startElementInfo.offset;
////    var endElementInfo = getEndElementInfoFromSelection(totalRange);
////    currentSelectionInfo.endElementPath=  endElementInfo.path;
////    currentSelectionInfo.endCharOffset= endElementInfo.offset;
////    highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, 0);
//    /********************* s : kindle way selection continue test */
//}

//function nativeTreeWalker(el, textNodesOnCurrentPage) {
//
//    var walker = document.createTreeWalker(
//        el,
//        NodeFilter.SHOW_TEXT,
//        null,
//        false
//    );
//
//    var node;
//    while(node = walker.nextNode()) {
//        textNodesOnCurrentPage.push(node);
//    }
//}

//function getElementDefaultDisplay(tagName) {
//    var cStyle,
//    t = document.createElement(tagName),
//    gcs = "getComputedStyle" in window;
//
//    document.body.appendChild(t);
//    cStyle = (gcs ? window.getComputedStyle(t, "") : t.currentStyle).display;
//    document.body.removeChild(t);
//
//    return cStyle;
//}

//function getLastTextWithinRange(rectList){
//
//    var lastLineData = new Array();
//
//    var largestTop = Math.max.apply(Math,rectList.map(function(obj){return obj.top;}))
//
//    for (var i=0; i<rectList.length; i++) {
//        if(rectList[i].top == largestTop){
//            lastLineData.push(rectList[i]);
//        }
//		}
//    var largestRight = Math.max.apply(Math,lastLineData.map(function(obj){return obj.right;}))
//
//    var lastTextData = lastLineData.find(function(obj){ return obj.right == largestRight; })
//    console.log("lastText : "+JSON.stringify(lastTextData));
//
//    return lastTextData;
//		}
/********************************************************* e : kindle way selection continue test */

/********************************************************* s : line annotation test */
var annotationLineRects= new Array();
function getAnnotationRects(){

    var flkTags = document.getElementsByTagName('flk');
    console.log("flkTags length: "+flkTags.length);

    var rect;
    var rectList = new Array();

    for(var i=0;i<flkTags.length;i++){
        var range = document.createRange();
        range.selectNodeContents(flkTags[i]);
        var rects = range.getClientRects();
        for(var j=0; j<rects.length; j++){
            annotationLineRects.push(rects[j]);
        }
    }
}

function drawLineAnnotation(){

    var lineRect = new Array();

    var scrTop = $(document).scrollTop();
    var scrBottom = scrTop + $(window).height();

    if(annotationLineRects.length==0){
        return;
    }

    for(var idx=0; idx<annotationLineRects.length; idx++){
        if(annotationLineRects[idx].top>scrTop &&
            annotationLineRects[idx].bottom<scrBottom){
                lineRect.push(annotationLineRects[idx]);
        }
    }
    drawSelectionRect(lineRect,false);
}
/********************************************************* e : line annotation test */
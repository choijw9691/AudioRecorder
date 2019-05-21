const SEARCH_HIGHLIGHT='KYBSearchHighlight';
const ELEMENT_NODE = 1;
const TEXT_NODE = 3;
const COMMENT_NODE = 8;
const HIGHLIGHT_CLASS='KYBH';
const ANNOTATION_LASTSPAN_CLASS='KYBAnnotatedLastSpan';
const MEMO_CLASS='KYBMemo';

var gWindowInnerHeight = 0;
var gWindowInnerWidth = 0;
var gNoteRefArray = new Array;

$(document).ready(function(){

    if (gWindowInnerHeight <= 0){
    	if (window.devicePixelRatio < 1.0) {
        	gWindowInnerHeight = window.outerHeight;
        } else {
        	gWindowInnerHeight = window.innerHeight;
        }
    }

    if (gWindowInnerWidth <= 0){
    	if (window.devicePixelRatio < 1.0) {
        	gWindowInnerWidth = window.outerWidth;
        } else {
        	gWindowInnerWidth = window.innerWidth;
        }
    }

    window.fixedlayout.setBGMState();

//    $('#feelingk_bookcontent').on('scroll touchmove',function(e){
//        if(isPreventMediaControl || textSelectionMode){
//            e.preventDefault();
//            e.stopPropagation();
//            return false;
//        } else {
//            finishTextSelection();
//        }
//    });

	document.addEventListener('scroll', function(event){
	   if(isPreventMediaControl || textSelectionMode){
            event.preventDefault();
            event.stopPropagation();
            return false;
	   } else {
            clearSelection();
	   }
	},false);

//    var viewportmeta = document.querySelector('meta[name="viewport"]');
//    var check = viewportmeta.content.match(/height=[^,]+/);
//    var heightValue  = check[0].match(/\d/g);
//    heightValue = heightValue.join("");
//    $('#feelingk_booktable').css('height',heightValue,'important');
//    $('#feelingk_bookcontent').css('height',heightValue,'important');

	 $('video').bind('webkitfullscreenchange mozfullscreenchange fullscreenchange', function(e) {

		 var state = document.fullScreen || document.mozFullScreen || document.webkitIsFullScreen;
		 var event = state ? 'FullscreenOn' : 'FullscreenOff';

		 document.webkitCancelFullScreen();

		 if(state) {
			 playVideoFullScreen($(this)[0]);
		 }
	 });

	 $('video').bind('play', function(){

		 if(isPreventMediaControl){
			 $(this)[0].pause();
			 window.fixedlayout.didPlayPreventMedia($($(this)[0]).getPath(), "video");
			 return false;
		 }

		 var osVersion = getAndroidOsVersion();
		 if(osVersion.charAt(0)<=4 && osVersion.charAt(2)<=0){	//ics만 fullscreen으로 재생되도록 함 4.0~4.0.4
			 playVideoFullScreen($(this)[0]);
		 }

		 window.fixedlayout.stopMediaOverlay();	//영상 재생 시 미디어오버레이 중지
	 });

    var audios = $('audio');
    for(var i = 0; i < audios.length; i++){
        log("audio pause : " + i);
        audios[i].pause();
    }

	 $('audio').bind('play', function(){

		 if(isPreventMediaControl){
			 $(this)[0].pause();
			 window.fixedlayout.didPlayPreventMedia($($(this)[0]).getPath(), "audio");
			 return false;
		 }

		 var audios = $('audio');
		 for(var i = 0; i < audios.length; i++) {
			 var audioElement = audios[i];
			 if( audioElement == this )
				 continue;
			 log("pause audio : " +i);
			 audioElement.pause();
		 }

		 window.fixedlayout.stopMediaOverlay();	// 음원 재생 시 미디어오버레이 중지
	 });

	 var epubtype = $.epubtype();
	 var epubswitch = $.epubswitch();
	 var epubtrigger = $.epubtrigger();
	 epubtype.init();
	 epubswitch.init();
	 epubtrigger.init();
	 gNoteRefArray = epubtype.data();

    resizeWidth();

	jQuery.fn.getPath=function() {

        if(this.length!=1) throw'Requires one element.';

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

    jQuery.fn.removeHighlight = function() {
        return this.find("span." + SEARCH_HIGHLIGHT).each(function() {
            this.parentNode.firstChild.nodeName;
            with (this.parentNode) {
                replaceChild(this.firstChild, this);
                normalize();
            }
        }).end();
    };
});

function log(text){
    if(window.fixedlayout && window.fixedlayout.print) {
        window.fixedlayout.print(text);
    }
}

//function getSvgTag() {
//    //모든 SVG 찾기
//    var svgs = $('svg');
//    for (var i = 0; i < svgs.length; i++) {
//        var svgElement = svgs[i];
//        svgReload(svgElement);
//    }
//}
//function svgReload(element) {
//    var node = $(element);
//    var htmlText = new XMLSerializer().serializeToString(element);
//    var prev = node.prev();
//    var next = node.next();
//    var parent = node.parent();
//    node.remove();
//
//    //상위형제
//    if (prev != null && prev.length > 0) {
//        prev.after(htmlText);
//    }
//    //하위형제
//    else if (next != null && next.length > 0) {
//        next.before(htmlText);
//    }
//    //부모
//    else {
//        parent.append(htmlText);
//    }
//}

//function checkAudioTag(){
//	//모든 오디오 찾기
//	var audios = $('audio');
//	for(var i = 0; i < audios.length; i++) {
//		var audioElement = audios[i];
//
//		//오디오 정지
//		audioElement.pause();
//
//		//결과값에 SRC 추가
//		var valueSRC = "";
//		if(audioElement.src.length > 0){
//			valueSRC = audioElement.src;
//		} else {
//			for(var j = 0; j < audioElement.children.length; j++){
//				if(audioElement.children[j].src.length > 0){
//					valueSRC = audioElement.children[j].src;
//					break;
//				}
//			}
//		}
//
//		if(valueSRC.length > 0){
//			if(audioElement.autoplay == true){
//				audioElement.play();
//				break;
//			}
//		}
//	}
//}

//function getVideoTag() {
//	//모든 비디오 찾기
//	var videos = $('video');
//	var videoArray = new Array();
//
//	for(var i = 0; i < videos.length; i++){
//		var videoElement = videos[i];
//
//		//비디오 정지
//		videoElement.pause();
//
//		var posLeft = $(videoElement).offset().left - $(document).scrollLeft();
//		var posTop = $(videoElement).offset().top - $(document).scrollTop();
//		var posRight = posLeft + $(videoElement).width();
//		var posBottom = posTop + $(videoElement).height();
//
////		if(posLeft >= 0 && posRight <= gWindowInnerWidth)
////		{
//		if(posLeft >= 0) {
//			//결과값에 SRC 추가
//			var valueSRC = "";
//			if(videoElement.src.length > 0) {
//				valueSRC = videoElement.src;
//			} else {
//				for(var j = 0; j < videoElement.children.length; j++) {
//					if(videoElement.children[j].src.length > 0) {
//						valueSRC = videoElement.children[j].src;
//						if( videoElement.children[j].src.indexOf(".mp4") > -1 ){
//							break;
//						}
//					}
//				}
//			}
//
//			if(valueSRC.length > 0){
//				if(videoElement.autoplay == true){
//					videoElement.play();
//					break;
//				}
//			}
//		}
//	}
//}

function stopAllMedia(){
	stopAllVideo();
	stopAllAudio();
}

function stopAllVideo(){
	var videos = $('video');
	for(var i = 0; i < videos.length; i++){
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

function getCurrentPagePath() {

    var pathObj = new Object();
    var elementPath = getFirstVisibleItem();

    if( elementPath !== null ) {

    	var node = $(elementPath)[0];
        var snippet = '';
        var isText = false;
        var tagName="";

    	if( node.textContent==undefined ) {
            var child = node.childNodes[0];
            if( child.tagName.toUpperCase() =='IMG' ) {
            	tagName = child.tagName;
    			snippet = decodeURI(child.src);
            } else if( child.tagName.toUpperCase() =='AUDIO' ) {
            	tagName = child.tagName;
    			snippet = decodeURI(child.src);
            }  else if( child.tagName.toUpperCase() =='VIDEO' ) {
            	tagName = child.tagName;
    			snippet = decodeURI(child.src);
    			if(snippet=="")
            		snippet = $(child).children('source').attr('src');
            }
    	} else {
            if( node.tagName.toUpperCase() == 'IMG' ) {
            	tagName = node.tagName;
            	snippet = decodeURI(node.src);
            } else if( node.tagName.toUpperCase() == 'AUDIO' ) {
            	tagName = node.tagName;
            	snippet = decodeURI(node.src);
            } else if( node.tagName.toUpperCase() == 'VIDEO' ) {
            	tagName = node.tagName;
            	snippet = decodeURI(node.src);
            	if(snippet=="")
            		snippet = $(node).children('source').attr('src');
            } else {
            	isText=true;
            	snippet = node.textContent;
            }
    	}

    	if( isText && snippet.length > 100 )
    		snippet = snippet.substring(0, 100);

    	pathObj.elementPath = elementPath;
    	pathObj.elementText = snippet;
    	pathObj.tagName = tagName;

        window.fixedlayout.reportCurrentPagePath(JSON.stringify(pathObj));
    }
    else {
    	window.fixedlayout.reportError(2);
        window.fixedlayout.reportCurrentPagePath(null);
    }
}

function getFirstVisibleItem() {

	try {
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
    				$(node).hasClass(HIGHLIGHT_CLASS) != true &&
    				$(node).hasClass(SEARCH_HIGHLIGHT) != true &&
    				node.tagName.toUpperCase() !== 'BR') {
				retval = $(node).getPath();
				break;
			}
		}
		return retval;
	} catch(err) {
		log('getFirstVisibleItem() - ' + err);
	}
	return null;
}

function removeSearchHighlight(){
	$('body').removeHighlight();
}

function searchTextByKeywordIndex(keyword,keywordIndex) {
	var element = $('body')[0];
	var keyIdx = keywordIndex;
	for(var i=0; i<element.childNodes.length; i++) {
		var node = element.childNodes[i];
		var key = keyword.toUpperCase();
		var result = innerSearchByKeywordIndex(node, key, keyIdx);
		if(result == -1){
			break;
		}
		keyIdx = result;
	}
}

function innerSearchByKeywordIndex(node, keyword, keywordIndex) {

	var keyIdx = keywordIndex;

	if( node.nodeType == 3 ) {

		var pos = node.data.toUpperCase().indexOf(keyword);

		while( pos >= 0 ) {

			keyIdx -= 1;
			if(keyIdx == -1){
				var spannode = document.createElement('span');
		        spannode.className = SEARCH_HIGHLIGHT;
		        var middlebit = node.splitText(pos);
		        var endbit = middlebit.splitText(keyword.length);
		        var middleclone = middlebit.cloneNode(true);
		        spannode.appendChild(middleclone);
		        middlebit.parentNode.replaceChild(spannode, middlebit);
		        $(spannode).css('background-color', '#FFC000', '!important');
			    var left = $(spannode).offset().left;
				return keyIdx;
			}
			pos = node.data.toUpperCase().indexOf(keyword, pos+1);
		}
	} else if (node.nodeType == 1 && node.childNodes && !/(script|style)/i.test(node.tagName)) {
		for(var i=0; i<node.childNodes.length; i++) {
			var result = innerSearchByKeywordIndex(node.childNodes[i], keyword, keyIdx);
			keyIdx = result;
			if(result == -1){
				return keyIdx;
			}
		}
	}
	return keyIdx;
}

function getRectangleObject(left,top,width,height){
  var rect={'left':left,'top':top,'width':width,'height':height};
  return rect;
}

function getElementRect(element) {
    return getRectangleObject(element.offsetLeft, element.offsetTop, element.offsetWidth, element.offsetHeight);
}

//텍스트 노드의 조상 엘리먼트를 가져온다
function getAncestorElementForTextNode(node) {
    var element = node;
    if (element.nodeType != ELEMENT_NODE) {
        element = element.parentNode;
    }
    return element;
}

function generateUniqueClass(){
	var randID='kyoboId'+Math.floor(Math.random()*10001);
	while($("."+randID).length)
		randID='kyoboId'+Math.floor(Math.random()*10001);
	return randID;
}

function stringDotRevision(StrPath){    // TODO :: 수정하기
	StrPathOne = StrPath.replace(/\\\./g,'.');
	StrPathTwo = StrPathOne.replace(/\./g,'\\.');
	return StrPathTwo;
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

function setRangeStart(range, startContainer, startOffset){
    range.setStart(startContainer, startOffset);
}

function setRangeEnd(range, endContainer, endOffset){
    range.setEnd(endContainer, endOffset);
}

function setPointInRange(node, charOffset, range, setter){

    if(node===null) {
        return false;
    }

    if(charOffset===0) {
        setter(range,node,0);
        return true;
    }

//    while(node.textContent.length<charOffset) {
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
            }
            else if(node.nodeType===TEXT_NODE) {
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
    if(node.nodeType===ELEMENT_NODE) {
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
            } else{
                cumulativeOffset=newOffset;
            }
        }
    }
    return false;
}

function firstAncestralSibling(node) {

    if(node===null||node.parentNode===null){ return null; }

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

function checkContainSVG(range)  {

    var selectionHtml = "";
    var container = document.createElement("div");
    container.appendChild(range.cloneContents());
    selectionHtml = container.innerHTML.toLowerCase();

    if (selectionHtml.indexOf('<svg') != -1)
        return true;
    else
        return false;
}

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

function nextNode(node) {
    if(node===null){return null;}
    var result=null;
    if(node.nodeType==ELEMENT_NODE && node.childNodes.length>0){
        result=node.childNodes[0];
    } else {
        result=firstAncestralSibling(node);
    }
    return result;
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

function highlightSpans(spans, deleted, isAnnotation, highlightID, colorIndex) {

	var spanNum=0;
	var blankNodes=0;
    for(var i=0;i<spans.length;i++){
    	var outerSpan = spans[i];
    	outerSpan.title = highlightID;
    	$(outerSpan).addClass(highlightID);
    	$(outerSpan).addClass(HIGHLIGHT_CLASS);
        $(outerSpan).addClass('FLKAnnotationColor'+colorIndex);
//        $(outerSpan).addClass('FLKAnnotationFontColor');	// 주석 폰트 색상은 무조건 검정으로 -> TODO :: 20190219 정책 변경됨 - 모든 폰트 컬러 원본 유지
    }

    if (spans.length > 0) {
        $(spans[spans.length-1]).addClass(ANNOTATION_LASTSPAN_CLASS);
    }
}

function cullBlankTextNodesAroundSpan(span) {
    var parent=span.parentNode;
    var i=parent.indexOf(span);

    if(i>0) {
        var nodeBefore=parent.childNodes[i-1];
        if(nodeBefore.nodeType==TEXT_NODE&&nodeBefore.nodeValue==='') {
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
                childSpans.push(childSpan);
                ++childCount;
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
            if(range.endContainer.childNodes.length>range.endOffset){
                endNode=range.endContainer.childNodes[range.endOffset];
            }
        } else if(range.endOffset<range.endContainer.nodeValue.length) {
            endNode=range.endContainer;
        }

        while(currentNode!==null&&currentNode!=endNode) {
        	if(currentNode.nodeType===TEXT_NODE&&currentNode.textContent!==''
                && currentNode.textContent.trim() !== '') // Jeong, 2013-10-30 : 개행노드나 공백노드 제외 //&&currentNode.textContent!=='\n'&&currentNode.textContent!=='\r\n')
            {
                textNodes.push(currentNode);
            }
            currentNode=nextNode(currentNode);
        }
    } catch (err) {
        log("findAllFullTextNodesInRange err : "+err);
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

function saveHighlight(highlight) {
    if(highlightIsValid(highlight)){
        window.fixedlayout.saveHighlight(JSON.stringify(highlight));
        return true;
    }
    return false;
}

function highlightIsValid(highlight){
    return highlight!==null;
}

function getWindowWidth(twoPageViewMode) {  // TODO :: 값 확인
    if (twoPageViewMode == 1) {
        var width = gWindowInnerWidth + Math.ceil(getColumnGap(twoPageViewMode)/parseFloat(getNumColumns(twoPageViewMode)));
        if (width % getNumColumns(twoPageViewMode) != 0) {
            width = width - (getNumColumns(twoPageViewMode)-1);
        }
        return width;
    } else {
        return gWindowInnerWidth;
    }
}

function getNumColumns(twoPageViewMode){    // TODO :: 픽스드는 필요 없어 보임
    if (twoPageViewMode == 1) {
        return 2;
    } else{
        return 1;
    }
}

function getColumnGap(twoPageViewMode) {     // TODO :: 픽스드는 필요 없어 보임
    if (twoPageViewMode == 1) {
        return 0;
    } else {
        return 0;
    }
}

function deleteHighlights( highlights) {    // TODO :: 프론트에서 쓰이는 경우 있는지 확인하고 안쓰면 없애기

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

	        window.fixedlayout.reportHighlightPosition(id,0,0,0,0,false);
	    }
	    setMemoIcon();
	}
	catch(err) {
		log('deleteHighlights : ' + err);
	}

}

//function changeHighlightColorDirect(highlightID, clrIndex, callBack) {
//
//	var percent;
//
//	try {
//		var highlightSpans=$("[title=\""+highlightID+"\"]");
//
//		for(var i=0; i<highlightSpans.length; i++) {
//			var span = highlightSpans[i];
//			var className = $(span).attr('class');
////			$(span).removeClass(className);
//			$(span).removeClass('FLKAnnotationColor0 FLKAnnotationColor1 FLKAnnotationColor2 FLKAnnotationColor3 FLKAnnotationColor4 FLKAnnotationColor5');
////			$(span).addClass(HIGHLIGHT_CLASS);
//			$(span).addClass('FLKAnnotationColor'+clrIndex);
//
//			if(i==0){
//				percent = getPercentOfElement($(span));
//			}
//		}
//
//		if(callBack)
//			window.fixedlayout.changeHighlightColor(highlightID, clrIndex, percent);
//
//	} catch(err) {
//		log("changeHighlightColorDirect err : "+err);
//	}
//}

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
        console.log("applyHighlights err : "+err);
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
        console.log("applyHighlight err : "+err);
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
	}
	catch(err) {
		log('deleteAllHighlights : ' + err);
	}
}

function scrollToElement(element) {

    var node = element.parent();
    var role = node.attr("role");

    if(role!=null && role!=undefined){
    	if (role.toLowerCase().indexOf("scrollitem")!=-1) {
    		while(1) {
    			var node = node.parent();
    			var attr = node.attr("role");
    			if(attr!=null && attr!=undefined){
    				if (attr.toLowerCase().indexOf("scrollitem")==-1 && attr.toLowerCase().indexOf("scroll")!=-1)
        				break;
    			}
    			if (node[0].nodeName == "body")
    				return;
    		}
    		var top = node.offset().top;
    		var scrollTop = node.scrollTop();
    		var nodeTop = element.offset().top;
    		node.scrollTop(scrollTop + nodeTop - top);
    	}
    } else {
        return;
    }
}

function scrollToSearch() {
    scrollToElement($(".KYBSearchHighlight"));
}

function scrollToAnnotationID(id) {
	scrollToElement($("[title=\""+id+"\"]"));
}

Element.prototype.indexOf=function(elm){
    var nodeList=this.childNodes;
    var array=[].slice.call(nodeList,0);
    return array.indexOf(elm);
};

HTMLDocument.prototype.indexOf=function(elm){
    var nodeList=this.childNodes;
    var array=[].slice.call(nodeList,0);
    return array.indexOf(elm);
};

/**************************************************** s:TTS */
function getPathOfFirstElement(ttsDataJsonArr) {

	var currentXPath = getFirstTTSElementPath(ttsDataJsonArr);

    if( currentXPath == null || currentXPath == "" ){
        window.ttsDataInfo.setTTSCurrentIndex(null);
    } else{
        window.ttsDataInfo.setTTSCurrentIndex(currentXPath);
    }
}

function getFirstTTSElementPath(ttsDataJsonArr) {
	if( ttsDataJsonArr.length > 0 ) {
		for ( var i = 0; i < ttsDataJsonArr.length; i++) {
			if ($(ttsDataJsonArr[i]).css('display') == "none")
                continue;
            return ttsDataJsonArr[i];
		}
        return ttsDataJsonArr[0];
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

function getFirstSentence(ttsDataArray, path, listStartIndex) {
    window.ttsDataInfo.setStartPosition(path, 0, listStartIndex);
}

function setTTSHighlight(ttsData) {

	var currentNode = $(ttsData.path)[0];

    var role =$(ttsData.path).attr("role");

    if (role!=null && role!=undefined){
    	if(role.toLowerCase().indexOf("scroll")!=-1  || role.toLowerCase().indexOf("scrollitem")!=-1){
        	window.highlighter.requestHighlightRect(null, false);
        	return;
    	}
    }

	if (currentNode.tagName.toLowerCase() == "img" || currentNode.tagName.toLowerCase() == "video" || currentNode.tagName.toLowerCase() == "audio"
		  || currentNode.tagName.toLowerCase() == "svg" || currentNode.tagName.toLowerCase() == "math") {
        var clientRects = currentNode.getClientRects();
        var rects = new Array();
        var rect = getRectangleObject(document.body.scrollLeft + clientRects[0].left, clientRects[0].top, clientRects[0].width, clientRects[0].height);
        if (rect != null)
            rects.push(rect);
        var result = new Object();
        result.bounds = rects;
        result.filePath = ttsData.filePath;
        var nextPage = false;
        window.highlighter.requestHighlightRect(JSON.stringify(result), nextPage);
        return;
    }

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
    	var valid = true;
    	var exist = false;

    	for( var j = 0; j < rects.length; j++ ){
    		var rect = rects[j];
    		if( (document.body.scrollLeft + clientRect.left) == rect.left && clientRect.top == rect.top && clientRect.width == rect.width && clientRect.height == rect.height ){ //똑같은 rect 값을 걸러주기 위함
    			exist = true;
    			break;
    		}
    	}

    	if ( notSame && !exist ){
	    	for( var j = 0; j < endRects.length; j++ ) {
	    		var rect = endRects[j];
	    		if( clientRect.left == rect.left && clientRect.top == rect.top && clientRect.width == rect.width && clientRect.height == rect.height ){ //end element rect와 똑같은 rect 값을 걸러주기 위함
	    			endRects.splice(j,1);
	    			valid = false;
	    			break;
	    		}
	    	}
    	}

    	if( valid && !exist )
    		rects.push( getRectangleObject(document.body.scrollLeft + clientRect.left, clientRect.top, clientRect.width, clientRect.height) );
	}

	var result = new Object();
	result.bounds = rects;
    result.filePath = ttsData.filePath;
    window.highlighter.requestHighlightRect(JSON.stringify(result), false);
}

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

function clearSelection(){
    textSelectionMode = false;
    window.fixedlayout.finishTextSelectionMode();
}

var isPreventMediaControl=false;
function setPreventMediaControl(isPrevent){

	isPreventMediaControl = isPrevent;

	var audios = $('audio');
	var videos = $('video');

	for(var i = 0; i < audios.length; i++) {
		audios[i].pause();
	}
	for(var i = 0; i < videos.length; i++) {
		videos[i].pause();
	}
}

function playVideoFullScreen(videoElement){

	var valueSRC="";

	for(var i = 0; i < videoElement.children.length; i++) {
		if(videoElement.children[i].src.length > 0) {
			valueSRC = videoElement.children[i].src;
			if( videoElement.children[i].src.indexOf(".mp4") > -1 ){
				break;
			}
		}
	}

	 var osVersion = getAndroidOsVersion();
	 if(osVersion.charAt(0)>=7){
		 window.fixedlayout.reportVideoInfo(valueSRC);	// 누가 예외처리
	 } else{
		 window.fixedlayout.playVideo(valueSRC);
	 }
}

function getAndroidOsVersion() {
    var userAgent = navigator.userAgent.toLowerCase();
    var check = userAgent.match(/android\s([0-9\.]*)/);
    return check ? check[1] : false;
}

function getIDListByPoint(x, y, filePath) {

	var element=document.elementFromPoint(x, y);
    var idList = new Array();

    while (element.id != "feelingk_bookcontent" && element.tagName.toLowerCase() != "body"){
    	if (element.id != "" && element.id != undefined) {
    		idList.push(filePath + "#" + element.id);
    	}
    	element = element.parentElement;
    }
    window.fixedlayout.setIdListByPoint(JSON.stringify(idList));
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

/** element id list 중 현재 페이지 첫번째 element 구하는 함수 */
function getIDofFirstVisibleElement(filePath, ids){

    var docscrollleft = $(document).scrollLeft();
    var viewportWidth = $(window).width();
    var scrLeft = docscrollleft;
    var scrRight = docscrollleft + viewportWidth;

    for (var i=0; i<ids.length; ++i) {
        var node = $("#" + ids[i])[0];
        var leftOffset = $(node).offset().left;
        if (leftOffset >= scrLeft && leftOffset < scrRight){
        	window.mediaoverlay.setIDofFirstVisibleElement(filePath, ids[i]);
        	break;
        }
    }
}

function addMediaOverlayHighlight(id, activeclass, playbackclass) {

	removeMediaOverlayHighlight(activeclass, playbackclass);

	$('#'+id).addClass(activeclass);

	if(playbackclass!=""){
		$('#feelingk_bookcontent').find('*').addClass(playbackclass);
		$('#'+id).removeClass(playbackclass);
	}
}

function removeMediaOverlayHighlight(activeclass, playbackclass){
	$('#feelingk_bookcontent').find('*').removeClass(activeclass);
	$('#feelingk_bookcontent').find('*').removeClass(playbackclass);
}

function reloadPoster(){

	 var videos = $('video');

	 for(var i = 0; i < videos.length; i++){
		 var posterUrl = videos[i].poster;
		 if(posterUrl!=null && posterUrl!=""){
			 document.getElementsByTagName("video")[0].setAttribute("poster", posterUrl);
		 } else {
			 videos[i].currentTime = 1;
		 }
	 }
}

function hideNoteref(){
	noteref.hide();
	window.fixedlayout.setAsidePopupStatus(false);
}

function setPreventNoteref(isPrevent){
	noteref.setPrevent(isPrevent);
}

function getPercentOfElement(element) {     // TODO :: 색상 변경 시 쓰였는데 어디다 써야하는지 확인하기

    var elementTop = element.offset().top;
    var elementLeft = element.offset().left;
    var body = $('body')[0];

    var totalRect = body.scrollWidth * body.scrollHeight;
	var top = elementTop + body.scrollTop;
	var left = elementLeft
	var topRect = top * window.innerWidth;
	var rect = topRect + left;

	var percent = rect / totalRect * 100;
	return percent;
}

function getPercentOfRange(range) {

    var clientRect = range.getClientRects()[0];
    var body = $('body')[0];

    if (clientRect) {
    	var totalRect = body.scrollWidth * body.scrollHeight;
    	var top = clientRect.top + body.scrollTop;
    	var left = clientRect.left;
    	var topRect = top * window.innerWidth;
    	var rect = topRect + left;
    	var percent = rect / totalRect * 100;
    	return percent;
    } else{
    	return -1;
    }
}

function setMemoIcon() {

    $(".icon_memo").remove();

    var iconFilePath =  window.fixedlayout.getMemoIconPath();

    var memos = $("." + MEMO_CLASS + "." + ANNOTATION_LASTSPAN_CLASS);

    var iconWidth = 0;
    var iconHeight = 0;

    if (window.innerWidth < 800) {
        iconWidth = 17;
        iconHeight = 17;
    } else if (window.innerWidth < 1600) {
        iconWidth = 30;
        iconHeight = 30;
    } else if (window.innerWidth >= 1600) {
        iconWidth = 60;
        iconHeight = 60;
    }

    for (var i=0; i<memos.length; ++i) {
    	var clientRects = memos[i].getClientRects();
    	var lastClientRect = $(clientRects).last()[0];
    	var firstClienRect = $(memos[i].getClientRects())[0];
    	var bottom = lastClientRect.height;
    	var imgTag = "<img class='icon_memo' src='" + iconFilePath + "'"+ " style='bottom:" + bottom +"px; width:" + iconWidth + "px; height:" + iconHeight + "px;' />";

    	$(memos[i]).append(imgTag);
    }
}

function findTagUnderPoint(x, y, orgX, orgY, isSelectionDisabled){

    console.log("SSIN findTagUnderPoint in");

    noteref.isPrevent=false;

    var element = document.elementFromPoint(x, y);
    if(element==null || element==undefined)
        return;

    var tagName = element.tagName.toUpperCase();
    console.log("SSIN findTagUnderPoint tagName : "+tagName);

    if( noteref.isChanged ){
        noteref.isChanged  = false;
        return;
    } else if( !noteref.isChanged && noteref.status()){
        hideNoteref();
        return;
    }

    var isExceptionalTagOrAttr = false;
    if(!isSelectionDisabled){
        isExceptionalTagOrAttr = findHighlight(x, y, element);
    }

    if(element.hasAttribute('onclick')){
        return;
    }

    if( element.tagName.toUpperCase() == 'BUTTON' ||  tagName == 'TEXTAREA' || tagName == 'INPUT' || tagName == 'VIDEO' || tagName == 'AUDIO') {
        return;
    }

    if( tagName == 'IMG' || tagName == 'A') {
        if(tagName == 'A'){
            var href = element.href;
            if( href.length > 0 ){
                window.fixedlayout.reportLinkClick(href);
                return;
            }
        } else if(tagName == 'IMG'){
            var parentElement = element.parentNode;
            while(parentElement.id != "feelingk_bookcontent"){	// img 태그 부모가 a tag 인 경우를 위해 부모 검사
                if(parentElement.tagName.toUpperCase()=='A'){
                    var href = parentElement.href;
                    if( href.length > 0 ){
                        window.fixedlayout.reportLinkClick(href);
                        return;
                    }
                }
                parentElement = parentElement.parentNode;
            }
            var role = element.getAttribute('role');
            if(role!=null && role!=undefined && role.toLowerCase().indexOf("button") != -1){
                return;
            }
        }
    } else {
        var parent = element.parentElement;
        if(parent!==null && parent!==undefined) {
            var parentTagName = parent.tagName.toUpperCase();
            if( parentTagName == 'A') {
                var href = parent.href;
                if( href.length > 0 ){
                    window.fixedlayout.reportLinkClick(href);
                    return;
                }
            }

            if(parent.hasAttribute('onclick')){
                return;
            }

            var role = parent.getAttribute('role');
            if(role!=null && role!=undefined && role.toLowerCase().indexOf("button") != -1){
                return;
            }
        }

        var role = element.getAttribute('role');
        if(role!=null && role!=undefined && role.toLowerCase().indexOf("button") != -1){
            return;
        }
    }

    if(!isExceptionalTagOrAttr)
        window.fixedlayout.reportTouchPosition(orgX, orgY);
}


function findHighlight(x, y, element) {

    contextMenuTargetPosition="END";

    var highlightID=null;
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
        console.log("findHighlight : "+error);
	}
}

/************************************************************************************ [s : new custom selection]  */
const MOVE_PREV = 1;                    // 핸들러 움직이는 방향
const MOVE_MIDDLE = 2;
const MOVE_NEXT = 3;

var textSelectionMode=false;            // 셀렉션 모드 여부

var totalRange;                         // 실제 저장 될 셀렉션 range
var totalRangeTemp;                     // 핸들러 움직임 체크를 위한 임시 셀렉션 range

var startRange;                         // 롱프레스 시 단어 판단을 위한 단어 range

var selectionStartCharacterRange;       // 실제 움직임을 판단하기 위한 첫 글자 range
var isSameStartCharacter=true;

var currentSelectionInfo;
var currentSelectedHighlightId=null;    // 실제 움직임을 판단하기 위한 첫 글자 range

var contextMenuTargetPosition = "END";  // 컨텍스트 메뉴 기준 핸들러 포지션
/***************************************************** s : new custom selection - make totalRange with user action */
function setStartSelectionRange(x,y) {

    console.log("SSIN setStartSelectionRange in");

//    if(textSelectionMode){
//        textSelectionMode = false;
//        window.fixedlayout.finishTextSelectionMode();
//        return;
//    }

    textSelectionMode = true;
    currentSelectedHighlightId = null;

    var currentElement = document.elementFromPoint(x, y);
    if(!checkSelectionAvailable(currentElement, null)){
        window.fixedlayout.finishTextSelectionMode();
        return;
    }

    try {
        textSelectionMode = true;

        isSameStartCharacter = true;

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
    } catch(error){
        console.log("setStartSelectionRange error : "+error);
        window.fixedlayout.finishTextSelectionMode();
    }
}

function setMoveRange(x,y) {

    console.log("SSIN setMoveRange in");

    if(!textSelectionMode || currentSelectedHighlightId!=null) return;

    var currentElement = document.elementFromPoint(x, y);
    var moveRange = document.caretRangeFromPoint(x, y);
    if(!checkSelectionAvailable(currentElement, moveRange)){
        var rectList = getSelectedTextNodeRectList(totalRange);
        drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
        return;
    }

    try {

        var compare = startRange.compareBoundaryPoints(Range.START_TO_END, moveRange);   // 값은 startRange 기준으로 조건에 따라 나옴
        var startCompare = selectionStartCharacterRange.compareBoundaryPoints(Range.START_TO_START, moveRange);
        var endCompare = selectionStartCharacterRange.compareBoundaryPoints(Range.END_TO_END, moveRange);

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

        var rectList = getSelectedTextNodeRectList(totalRange);
        drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);

    } catch(error){
        console.log("setMoveRange error : "+error);
        window.fixedlayout.reportError(1);
    }
}

function setEndRange(colorIndex) {

    console.log("SSIN setEndRange in");

    if(!textSelectionMode) return;

    if(totalRange.toString().trim().length==0){
        textSelectionMode = false;
        window.fixedlayout.finishTextSelectionMode();
        return;
    }

    try {
        setSelectedText(totalRange.toString());

        totalRangeTemp = totalRange.cloneRange();

        if(!currentSelectionInfo.isExistHandler){
            // 롱프레스 시작 단어 밖에서 셀렉션 끝난 경우 - 퀵하이라이트
            var isMergeMemoAvailable=true;
            if(isExistAnnotationInRange(totalRange)){
                isMergeMemoAvailable = window.fixedlayout.checkMemoMaxLength(JSON.stringify( getAnnotationIdList()));
                if(isMergeMemoAvailable){
                    currentSelectionInfo=requestAnnotationInfo(totalRange, true);
                    highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
                    textSelectionMode = false;
                } else {
                    currentSelectionInfo.isExistHandler = true;
                    var rectList = getSelectedTextNodeRectList(totalRange);
                    drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
                    window.fixedlayout.overflowedMemoContent();
                    contextMenuTargetPosition = "END"
                    showCurrentContextMenu(null, 2, contextMenuTargetPosition);
                }
            } else {
                currentSelectionInfo=requestAnnotationInfo(totalRange, true);
                highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
                textSelectionMode = false;
            }
        } else {
            // 롱프레스 시작 단어 내에서 셀렉션 끝난 경우 - 셀렉션
            // check. 셀렉션 내 주석 포함 여부
            // - 포함 : 수정 메뉴 보여주기
            // - 미포함 : 신규 메뉴 보여주기
            if(isExistAnnotationInRange(totalRange)){
                showCurrentContextMenu(null, 2, contextMenuTargetPosition);
            } else {
                showCurrentContextMenu(null, 0, contextMenuTargetPosition);
            }
        }
    } catch(error){
        console.log("setEndRange error : "+error);
        window.fixedlayout.reportError(1);
    }
}

function setMoveRangeWithHandler(x, y, isStartHandlerTouched, isEndHandlerTouched){

    console.log("setMoveRangeWithHandler in");

    if(!textSelectionMode) {
        if(totalRange.toString().trim().length > 0){
            var rectList = getSelectedTextNodeRectList(totalRange);
            drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
        }
        return;
    }

    var currentElement = document.elementFromPoint(x, y);
    var moveRange = document.caretRangeFromPoint(x, y);

    if(!checkSelectionAvailable(currentElement, moveRange)){
        console.log("SSIN setMoveRangeWithHandler checkSelectionAvailable false");
        var rectList = getSelectedTextNodeRectList(totalRange);
        drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
        return;
    }

    try {
        if(moveRange != null) {
            var movePositionResult = positionCheck(x, y, moveRange);
            if(movePositionResult==null) {
                var rectList = getSelectedTextNodeRectList(totalRange);
                drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
                return;
            }
            var movingRange = movePositionResult.movingRange;
            if(movePositionResult.movePosition == MOVE_PREV) {
                contextMenuTargetPosition = "START";
                if(isStartHandlerTouched) {
                    totalRange.setStart(movingRange.startContainer, movingRange.startOffset);
                    totalRange.setEnd(totalRangeTemp.endContainer, totalRangeTemp.endOffset);
                } else if(isEndHandlerTouched) {
                    totalRange.setStart(movingRange.startContainer, movingRange.startOffset);
                    totalRange.setEnd(totalRangeTemp.startContainer, totalRangeTemp.startOffset);
                }
            } else if(movePositionResult.movePosition == MOVE_NEXT) {
                contextMenuTargetPosition = "END";
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
        }
        var rectList = getSelectedTextNodeRectList(totalRange);
        drawSelectionRect(rectList, currentSelectionInfo.isExistHandler);
    } catch(error){
        console.log("setMoveRangeWithHandler error : "+error);
        window.fixedlayout.reportError(1);
    }
}

function positionCheck(x,y,movingRange) {

    var move_position = MOVE_PREV;

    var touchRangeStartElement = totalRangeTemp.startContainer;
    var touchRangeEndElement = totalRangeTemp.endContainer;

    if(movingRange != null) {
        var startCompare = movingRange.compareBoundaryPoints(Range.START_TO_START, totalRangeTemp);
        var endCompare = movingRange.compareBoundaryPoints (Range.END_TO_END, totalRangeTemp);

        if(startCompare == -1 && endCompare == -1) {
            move_position = MOVE_PREV;
        } else if(startCompare == 1 && endCompare == 1) {
            move_position = MOVE_NEXT;
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

function setEndRangeWithHandler(colorIndex) {

     console.log("setEndRangeWithHandler in ");

    if(!textSelectionMode)
        return;

    if(totalRange.toString().trim().length==0){
        textSelectionMode = false;
        window.fixedlayout.finishTextSelectionMode();
        return;
    }

    totalRangeTemp = totalRange.cloneRange();

    if(currentSelectedHighlightId!=null){
        // 재활성화 후 핸들러 이동 완료 시
        // check. 셀렉션 내 주석 포함 여부
        // - 포함 : 수정 메뉴 보여주기
        // - 미포함 : 신규 메뉴 보여주기
        if(isExistAnnotationInRange(totalRange)){
            showCurrentContextMenu(null, 2, contextMenuTargetPosition);
        } else {
            showCurrentContextMenu(null, 0, contextMenuTargetPosition);
        }
    } else {
        // 신규 핸들러 이동 완료 시
        // check. 이너셀렉션
        // - 이너셀렉션 : 수정 메뉴 보여주기
        // - 이너셀렉션 외 : 신규 메뉴 보여주기
        if(isExistAnnotationInRange(totalRange)){
            showCurrentContextMenu(null, 2, contextMenuTargetPosition);
        } else {
            showCurrentContextMenu(null, 0, contextMenuTargetPosition);
        }
    }

    setSelectedText(totalRange.toString());
}

function showCurrentContextMenu(highlightID, menuTypeIndex, contextMenuPosition){
    var tempSelectedRect = totalRange.getClientRects();
    var currentSelectedRect = [];
    for( var index = 0 ; index < tempSelectedRect.length; index++ ) {
        if(tempSelectedRect[index].width != 0){
            currentSelectedRect.push(tempSelectedRect[index]);
        }
    }
    var startRect = currentSelectedRect[0];
    var endRect = currentSelectedRect[currentSelectedRect.length-1];
    window.fixedlayout.showContextMenu( highlightID, menuTypeIndex, contextMenuPosition, endRect.right, endRect.top, endRect.bottom, startRect.left, startRect.top, startRect.bottom);
}

function showCurrentHighlightSelection(highlightID){

    var annotationElms = document.getElementsByClassName(highlightID);

    if(annotationElms.length == 0)
        return;

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
            if(touchRectList[i].x == touchRectList[j].x && touchRectList[i].y == touchRectList[j].y) {
                touchRectList.splice(j, 1);
            } else {
                j++;
            }
        }
    }

    drawSelectionRect(touchRectList, currentSelectionInfo.isExistHandler);

    totalRangeTemp = totalRange.cloneRange();

    setSelectedText(totalRange.toString());

    setTimeout(function () { showCurrentContextMenu(highlightID, 2, contextMenuTargetPosition);}, 100);
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
        },
false
    );

    var tempRange=document.createRange();
    tempRange.setStart(totalRange.startContainer, totalRange.startOffset);
    tempRange.setEnd(totalRange.startContainer, totalRange.startContainer.textContent.length);

    var rects = tempRange.getClientRects();
    for (var i = 0; i < rects.length; i += 1)
        textNodeRectList.push(rects[i]);

    var node;
    while ((node = nodeIterator.nextNode())) {
        if (totalRange.startContainer.compareDocumentPosition(node) !== Node.DOCUMENT_POSITION_PRECEDING && totalRange.startContainer !== node) {
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
    window.fixedlayout.drawSelectionRect(JSON.stringify(rectList), isExistHandler);
}

function requestAllMemoText(){
    var containTarget = getAnnotationIdList();
    window.fixedlayout.mergeAllMemoText(JSON.stringify(containTarget));
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
//    window.fixedlayout.setAnnotationIdList(JSON.stringify(containTarget));
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

//            var range=document.createRange();
//
//            if(!setPointInRange(startElement, startCharOffset, range, setRangeStart)) {
//                throw("Could not set start of selection range");
//            }
//
//            if(!setPointInRange(endElement, endCharOffset, range, setRangeEnd)) {
//                throw("Could not set end of selection range");
//            }

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

//            range.detach();

        	window.fixedlayout.checkMergeAnnotation( JSON.stringify(highlightToSave) );
        } else {
            throw("Snippet start offset is negative");
        }
    } catch(err) {
        console.log("error highlighting selection 2: "+err);
        window.fixedlayout.reportError(5);
        window.fixedlayout.finishedApplyingHighlight(false);
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

            // TODO :: 상위 태그 css 있는거 inherit 안되는거 수정해야함
//            var cs= getComputedStyle($('#kyoboId869')[0]);
//              for (var i=0 ; i<cs.length; i++) {
//                $('#kyoboId869').css(cs[i], 'inherit');
//              }

		    highlightToSave.percent = getPercentOfRange(range);

		    range.detach();

            var spanId = "";
            var lastId = '';
            highlightToSave.uniqueId = uniqueId;
            highlightToSave.spanId = spanId;
            highlightToSave.chapterId = lastId;
            highlightToSave.colorIndex = clrIndex;
            highlightToSave.text = selectedText;
            var memo = memoText[0];
            highlightToSave.memo = memo;
            saved=saveHighlight(highlightToSave);
            if(memo.length>0){
                $("[title=\""+highlightID+"\"]").addClass(MEMO_CLASS);
                setMemoIcon();
            }
        }
    } catch(err) {
        console.log("error highlighting selection 1: "+err);
        window.fixedlayout.reportError(5);
        window.fixedlayout.finishedApplyingHighlight(saved);
    }
}

function addAnnotation(colorIndex){

    if(totalRange == undefined || totalRange == null || totalRange.toString().length==0)
        return;

    var isMergeMemoAvailable=true;
    if(isExistAnnotationInRange(totalRange)){
        isMergeMemoAvailable = window.fixedlayout.checkMemoMaxLength(JSON.stringify( getAnnotationIdList()));
        if(isMergeMemoAvailable){
            var currentSelectionInfo=requestAnnotationInfo(totalRange, false);
            highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
            textSelectionMode = false;
        } else {
            window.fixedlayout.overflowedMemoContent();
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
         isMergeMemoAvailable = window.fixedlayout.checkMemoMaxLength(JSON.stringify( getAnnotationIdList()));
        if(isMergeMemoAvailable){
            var currentSelectionInfo = requestAnnotationInfo(totalRange, false);
            highlightFromSelection(currentSelectionInfo.startElementPath, currentSelectionInfo.endElementPath, currentSelectionInfo.startCharOffset, currentSelectionInfo.endCharOffset, colorIndex);
            textSelectionMode = false;
        } else {
            window.fixedlayout.overflowedMemoContent();
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

    var flkTags = document.getElementsByTagName('flk');
    for(var i=0; i<flkTags.length; i++){
        if(totalRange.intersectsNode(flkTags[i])){
            if($.inArray(flkTags[i].title, deleteTarget) === -1)
                deleteTarget.push(flkTags[i].title);
        }
    }

    for(var i=0; i<deleteTarget.length; i++){
        var highlightSpans=$("[title=\"" + deleteTarget[i] + "\"]");
        if( highlightSpans != null ) {
            $(highlightSpans).contents().unwrap();
        }
    }

    window.fixedlayout.addDeleteHistory(JSON.stringify(deleteTarget));

    setMemoIcon();
}

function checkInnerSelection(currentRange){
    var startElementParent = currentRange.startContainer.parentElement;
    var endElementParent = currentRange.endContainer.parentElement;
    var startElementTag = startElementParent.tagName.toLowerCase();
    var endElementTag = endElementParent.tagName.toLowerCase();
    var startElementTitle = startElementParent.title;
    var endElementTitle = endElementParent.title;currentRange
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

function checkSelectionAvailable(element, checkRange){
    if(element==null)
        return false;
    if(element.tagName.toUpperCase()=="HTML" || element.tagName.toUpperCase()=='BODY' || element.tagName.toUpperCase()=="IMG" || element.tagName.toUpperCase()=="AUDIO" || element.tagName.toUpperCase()=="VIDEO" ||
        $(element).getPath().toLowerCase().indexOf('>svg') != -1) { //  || element.id == "feelingk_booktable" || element.id == "feelingk_bookcontent"
            return false;
    } else if(checkRange!=null && checkRange.startContainer.nodeType!=TEXT_NODE){
        return false;
    } else {
        return true;
    }
}

function setSelectedText(selectedText){
    window.fixedlayout.setSelectedText(selectedText);
}

function finishTextSelection(){
    textSelectionMode = false;
}

function getSelectedElementPath() {

    if(totalRange == undefined || totalRange == null || totalRange.toString().length==0)
        return;

    var currentSelectionInfo = requestAnnotationInfo(totalRange, true);
    var startElementPath = getTTSPath($(currentSelectionInfo.startElementPath)[0]);
    var endElementPath = getTTSPath($(currentSelectionInfo.endElementPath)[0]);
    var startCharOffset = currentSelectionInfo.startCharOffset;
    var endCharOffset = currentSelectionInfo.endCharOffset;

	window.ttsDataInfo.setSelectedElementPath(startElementPath, endElementPath, startCharOffset, endCharOffset);
}

function removeCommentNode(){
    $('*').contents().each(function() {
        if(this.nodeType === Node.COMMENT_NODE) {
            $(this).remove();
        }
    });
}

function resizeWidth(){

    var viewportmeta = document.querySelector('meta[name="viewport"]');
    var check = viewportmeta.content.match(/width=[^,]+/);
    var viewportWidth  = check[0].match(/\d/g);
    viewportWidth = viewportWidth.join("");

    $('*').each(function() {
        if($(this).outerWidth()>viewportWidth){
            var diffWidth = $(this).outerWidth() - viewportWidth;
            $(this).css('width', (viewportWidth-diffWidth));
        }
    });
}
/********************************************************* e : selection common function test  */

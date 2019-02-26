(function ($) {
    $.bgmPlayer = {
	    isPlaying: false,
	    
	    playIndex: 0,
	    
        playBGM: function(playIndex) {
	        this.playIndex = playIndex;
			if (navigator.userAgent.match(/Android/i)) {
				if (window.bgmplayer != undefined){
					isPlaying = true;
					window.bgmplayer.playBGM(playIndex);
				}
				else
					console.log("undefined android bridge: bgmplayer");
			}
			else if (navigator.userAgent.match(/iPhone|iPad|iPod/i)) {
				window.location = "bgmplayer://" + "playBGM?index=" + index;
			}
        },
        
        pauseBGM: function() {
            if (navigator.userAgent.match(/Android/i)) {
	            if (window.bgmplayer != undefined){
	            	isPlaying = false;
					window.bgmplayer.pauseBGM();
	            }
	            else
					console.log("undefined android bridge: bgmplayer");
			}
			else if (navigator.userAgent.match(/iPhone|iPad|iPod/i)) {
				window.location = "bgmplayer://" + "pauseBGM";
			}
        },
        
        stopBGM: function() {
	        if (navigator.userAgent.match(/Android/i)) {
		        if (window.bgmplayer != undefined){
		        	isPlaying = false;
					window.bgmplayer.stopBGM();
		        }
				else
					console.log("undefined android bridge: bgmplayer");
			}
			else if (navigator.userAgent.match(/iPhone|iPad|iPod/i)) {
				window.location = "bgmplayer://" + "stopBGM";
			}
        },
        
        setBGMState: function(isPlaying, playIndex) {
	        this.isPlaying = isPlaying;
	        this.playIndex = playIndex;
	        
			if (typeof onChangedBGMState != "undefined")
				onChangedBGMState();
			else
				console.log("undefined test");
        }
    };
})(jQuery);
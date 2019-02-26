package com.ebook.epub.viewer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

// TODO :: 사용하지 않는 클래스 확인되면 삭제 예정
public class AnnotationManager {
	
	private String TAG = "AnnotationManager";

    /**
     * 주석 동기화 처리후 생성된 목록의 병합처리 
     *  - originalPath 와 mergePath 의 주석목록 파일로 병합처리후 historyPath의 경로에 병합한 파일을 생성한다.
     */
    public void mergeHighlight(String originalPath, String mergePath, String savePath ,String historyPath) {
    	
    	ArrayList<Highlight> originalArr = highlightsFromFile(originalPath);
    	ArrayList<Highlight> mergeArr = highlightsFromFile(mergePath);
    	
    	AnnotationHistory hlghHistory = new AnnotationHistory();
    	hlghHistory.read(historyPath);
    	
    	ArrayList<Highlight> highArr = new ArrayList<Highlight>();
    	ArrayList<Highlight> resultArr = new ArrayList<Highlight>();
    	
    	for(Highlight mh : mergeArr){
    		boolean isMatch = false;
    		for(Highlight oh : originalArr){
    			if(mh.uniqueID == oh.uniqueID){
    				isMatch = true;
    			}
    		}
    		
    		if(isMatch){
    			resultArr.add(mh);
    		}else{
    			highArr.add(mh);
    		}
    	}
    	
    	CheckHighlightResult result;
    	
    	for(int idx=0; idx < highArr.size(); idx++){
    		result = checkHighlight(highArr.get(idx), resultArr, hlghHistory, idx);
    		resultArr = result.resultArr;
    		hlghHistory = result.hlHistory;
    	}
    	
    	highlightsToFile(savePath, resultArr);
    	hlghHistory.write(historyPath);
    }
    
    /**
     * 하나의 주석이 전달받은 주석목록의 주석데이터와 
     * 영역이 겹치는 부분이 있는지 확인하여 병합처리한다.
     */
    private CheckHighlightResult checkHighlight(Highlight highlight, ArrayList<Highlight> highlightArr, AnnotationHistory hlghHistory, int index) {
    	
    	CheckHighlightResult result = new CheckHighlightResult();
    	ArrayList<Highlight> resultArr = new ArrayList<Highlight>();
    	
    	String hID = highlight.highlightID;
		String startPath = highlight.startPath;
		int startChildIndex = highlight.startChild;
		int startCharOffset = highlight.startChar;
		String endPath = highlight.endPath;
		int endChildIndex = highlight.endChild;
		int endCharOffset = highlight.endChar;
		int colorIndex = highlight.colorIndex;
		double percent = highlight.percent;
		String chapterName = highlight.chapterName;
		String annotationType = highlight.type;
		String text = highlight.text;
		String memoText = "";
		String extra1 = highlight.extra1;
		String extra2 = highlight.extra2;
		String extra3 = highlight.extra3;

		String current = highlight.chapterFile.toLowerCase();
		
		for(int i=0; i<highlightArr.size(); i++) {
			Highlight h = highlightArr.get(i);

			String file = h.chapterFile.toLowerCase();
			if( !current.equals(file) )
				continue;

			//뉴 셀렉션이 기 셀렉션에 포함되는 경우
			if(startChildIndex > h.startChild || (startChildIndex == h.startChild && startCharOffset >= h.startChar )){
				if(endChildIndex < h.endChild || (endChildIndex == h.endChild && endCharOffset <= h.endChar)){
					hlghHistory.mergeRemove(highlight.uniqueID, h.uniqueID);
					
					//메모가 있다면 병합
					if( highlight.isMemo() ){
						String newMemo = highlight.memo;
//						String newDevice = highlight.deviceModel;
//						String newDeviceInfo = "<[" + newDevice + "]>";
						if( h.isMemo() ){
//							String oldDeviceInfo = "<[" + h.deviceModel + "]>";
							h.memo = /*oldDeviceInfo +*/ h.memo + "\n" /*+ newDeviceInfo*/ + newMemo;
						} else {
							h.memo = newMemo;
						}
					}
					
					result.resultArr = highlightArr;
					result.hlHistory = hlghHistory;
					
					return result;
				}
			}
			
			//기 샐랙션이 뉴 셀렉션에 포함되는 경우
			if(startChildIndex < h.startChild || (startChildIndex == h.startChild && startCharOffset <= h.startChar )){
				if(endChildIndex > h.endChild || (endChildIndex == h.endChild && endCharOffset >= h.endChar)){
					DebugSet.d(TAG, "Include All !!!");
					resultArr.add(h);
					continue;
				}
			}
			//뉴 셀렉션이 한 문단 안에 있는 경우
			if( startChildIndex == endChildIndex && startPath.equals(endPath)) {
				
				// 뉴셀렉션이 기셀렉션을 감싼경우.
				if( (startChildIndex == h.startChild && startPath.equals(h.startPath) && endChildIndex == h.endChild && endPath.equals(h.endPath)) 
						&& startCharOffset <= h.startChar && endCharOffset >= h.endChar ) {
					DebugSet.d(TAG, "Include All !!!");
					resultArr.add(h);
				}
				////기존 셀렉션의 시작블럭이 같을경우 
				else if( startChildIndex == h.startChild && startPath.equals(h.startPath) ) {
					//기셀렉션도 한 문단인 경우 : Jeong, 2013-06-28 : iOS 추가병합로직
					if( endChildIndex == h.endChild ){
						//뉴셀렉션이 기셀렉션 앞쪽이나 같은지점에서 시작해 기셀렉션안에서 끝난경우.
						if( startCharOffset <= h.startChar && endCharOffset >= h.startChar 
								&& endCharOffset <= h.endChar ) {
							DebugSet.d(TAG, "PARTIAL startChild");
							resultArr.add(h);
							
							endPath = h.endPath;
							endChildIndex = h.endChild;
							endCharOffset = h.endChar;
						}
						//뉴셀렉션이 기셀렉션 안쪽이나 끝나는지점에서 시작해 기셀렉션보다 뒤쪽에서 끝난경우.
						else if( startCharOffset <= h.endChar && endCharOffset >= h.endChar ) {
							DebugSet.d(TAG, "PARTIAL endChild"); 
							resultArr.add(h);
							
							startPath = h.startPath;
							startChildIndex = h.startChild;
							startCharOffset = h.startChar;
						} 
						else if( startCharOffset >= h.startChar && endCharOffset <= h.endChar ){
							DebugSet.d(TAG, "INCLUDE 1");
							resultArr.add(h);
						}
						//기셀렉션이 멀티인 경우 : Jeong, 2013-06-28 : iOS 추가병합로직
					} else {
						//셀렉션의 시작이 기셀렉션 의 시작보다 뒤이고 셀렉션의 끝이 기셀렉션의 시작보다 뒤인 경우
                        if (startCharOffset <= h.startChar && endCharOffset >= h.startChar)
                        {
                        	resultArr.add(h);

                        	endPath = h.endPath;
							endChildIndex = h.endChild;
							endCharOffset = h.endChar;
                        } else if( startCharOffset >= h.startChar ){
                        	DebugSet.d(TAG, "INCLUDE 1");
							resultArr.add(h);
                        }
					}
				}
				//기존 셀렉션의 시작블럭이 더 앞일경우 
				else if( startChildIndex == h.endChild && startPath.equals(h.endPath)) {
					if( startCharOffset <= h.endChar && endCharOffset <= h.endChar ) {
						DebugSet.d(TAG, "INCLUDE 1");
						resultArr.add(h);
					} else if( startCharOffset <= h.endChar ) {
						DebugSet.d(TAG, "PARTIAL endChar");
						resultArr.add(h);
						
						startPath = h.startPath;
						startChildIndex = h.startChild;
						startCharOffset = h.startChar;
					}
				}
			}
			//뉴 셀렉션이 멀티 셀렉션인 경우
			else {
				//뉴셀렉션과 기셀렉션이 같은 문단에서 시작하는 경우
				if( startChildIndex == h.startChild && startPath.equals(h.startPath)) {
					//뉴 셀렉션의 시작문단과 끝 문단이 같은 경우
					if( h.startChild == h.endChild ) {
						if( startCharOffset < h.startChar ) {
							DebugSet.d(TAG, "INCLUDE 1");
							resultArr.add(h);
						}
						else if( h.endChar >= startCharOffset ) {
							DebugSet.d(TAG, "PARTIAL 1");
							resultArr.add(h);
							
							startPath = h.startPath;
							startChildIndex = h.startChild;
							startCharOffset = h.startChar;
						}
					}
					//뉴 셀렉션의 시작문단과 끝문단이 다른 경우
					else {
						if( startCharOffset < h.startChar ) {
							DebugSet.d(TAG, "INCLUDE 2");
							resultArr.add(h);
						}
						else if( h.startChar <= startCharOffset ) {
							DebugSet.d(TAG, "PARTIAL 2");
							resultArr.add(h);
							
							startPath = h.startPath;
							startChildIndex = h.startChild;
							startCharOffset = h.startChar;
						}
					}
				}
				//뉴 셀렉션의 시작문단과 기 셀렛션의 끝 문단이 같은경우
				else if( startChildIndex == h.endChild && startPath.equals(h.endPath)) {
					//기 셀렉션의 시작문단과 기 셀렉션의 끝문단이 같은 경우
					if( h.startChild == h.endChild ) {
						if( startCharOffset < h.startChar ) {
							DebugSet.d(TAG, "INCLUDE 3");
							resultArr.add(h);
						}
						else if( startCharOffset <= h.endChar ) {
							DebugSet.d(TAG, "PARTIAL 3");
							resultArr.add(h);
							
							endPath = h.endPath;
							endChildIndex = h.endChild;
							endCharOffset = h.endChar;
						}
					}
					//기셀렉션의 시작문단과 끝 문단이 다른경우
					else {
						if( startCharOffset <= h.endChar ) {
							DebugSet.d(TAG, "PARTIAL 4");
							resultArr.add(h);
							
							startPath = h.startPath;
							startChildIndex = h.startChild;
							startCharOffset = h.startChar;
						}
					}
				}
				else {
					DebugSet.d(TAG, "start =/= child");
				}
				
				//뉴 셀렉션의 끝 문단이 기 셀렉션의 시작 문단과 같은 경우
				if( endChildIndex == h.startChild && endPath.equals(h.startPath)) {
					//기 셀렉션의 시작문단과 끝문단이 같은경우
					if( h.startChild == h.endChild ) {
						if( endCharOffset > h.endChar ) {
							DebugSet.d(TAG, "INCLUDE 4");
							resultArr.add(h);
						}
						else if( h.startChar <= endCharOffset ) {
							DebugSet.d(TAG, "PARTIAL 5");
							resultArr.add(h);
							
							endPath = h.endPath;
							endChildIndex = h.endChild;
							endCharOffset = h.endChar;
						}
					}
					else {
						if( endCharOffset >= h.startChar ) {
							DebugSet.d(TAG, "PARTIAL 6");
							resultArr.add(h);
							
							endPath = h.endPath;
							endChildIndex = h.endChild;
							endCharOffset = h.endChar;
						}
					}
				}
				else if( endChildIndex == h.endChild && endPath.equals(h.endPath)) {
					if( endCharOffset > h.endChar ) {
						DebugSet.d(TAG, "INCLUDE 5");
						resultArr.add(h);
					}
					else if( endCharOffset <= h.endChar ) {
						DebugSet.d(TAG, "PARTIAL 7");
						resultArr.add(h);
						
						endPath = h.endPath;
						endChildIndex = h.endChild;
						endCharOffset = h.endChar;
					}
				}
				else {
					DebugSet.d(TAG, "end =/= child");
				}
				
			}
			
			if( startChildIndex < h.startChild && endChildIndex > h.endChild ) {
				DebugSet.i(TAG, "INCLUDE !!!");
//				resultArr.add(h);
			}
			else {
				if( h.startChild < startChildIndex && h.endChild > startChildIndex ) {
					DebugSet.i(TAG, "PARTIAL start !!");
					resultArr.add(h);
					
					startPath = h.startPath;
					startChildIndex = h.startChild;
					startCharOffset = h.startChar;
				}
				
				if( h.startChild < endChildIndex && h.endChild > endChildIndex ) {
					DebugSet.i(TAG, "PARTIAL end !!");
					resultArr.add(h);
					
					endPath = h.endPath;
					endChildIndex = h.endChild;
					endCharOffset = h.endChar;
				}
			}
//                Log.d(TAG, "____________________________________________________" );
		}
		
		if(resultArr.size() > 0){
			Highlight prevHighlight=null;
			text = "";
			ArrayList<String> memoList = new ArrayList<String>();
			ArrayList<String> deviceList = new ArrayList<String>();
			
			Highlight newHighlight = new Highlight();
			newHighlight.uniqueID = newHighlight.uniqueID + index;
			
			if( highlight.isMemo() ){
				memoList.add(highlight.memo);
				deviceList.add(highlight.deviceModel);
			}
			
			for(Highlight h: resultArr) {
				if( h != prevHighlight ) {
					
					if( h.isMemo() ) {
						memoList.add(h.memo);
						deviceList.add(h.deviceModel);
						annotationType = AnnotationConst.FLK_ANNOTATION_TYPE_MEMO;
					}
					
					prevHighlight = h;
				}
				for(int  i=0; i<highlightArr.size() ; i++) {
					Highlight high = highlightArr.get(i);
					if(high.uniqueID == h.uniqueID){
						highlightArr.remove(i);
						hlghHistory.mergeRemove(high.uniqueID, newHighlight.uniqueID);
						
						highlight.text = mergeHighlightText(h, highlight);
					}
				}
			}
			text = highlight.text;
			
			if( memoList.size() > 1  ){
				for (int i = 0; i < memoList.size(); i++) {
					String deviceInfo = "<[" + deviceList.get(i) + "]>";
//					memoText += deviceInfo + memoList.get(i);
					memoText += memoList.get(i);
					if( i < memoList.size()-1 )
						memoText += "\n";
				}
			} else if( memoList.size() == 0 ) {
				memoText = "";
			} else {
				memoText = memoList.get(0);
			}
			
			highlightArr.remove(highlight);
			hlghHistory.mergeRemove(highlight.uniqueID, newHighlight.uniqueID);
			
			highlight = new Highlight();
			highlight.uniqueID = newHighlight.uniqueID;
			highlight.highlightID = hID;
			highlight.startPath = startPath;
			highlight.startChild = startChildIndex;
			highlight.startChar = startCharOffset;
			highlight.endPath = endPath;
			highlight.endChild = endChildIndex;
			highlight.endChar = endCharOffset;
			highlight.colorIndex = colorIndex;
			highlight.memo = memoText;
			//2014.02.10
			//병합 후 주석메 메모일 경우 메모 색상으로 변경.
			if( highlight.memo.length() > 0 ) {
				highlight.colorIndex = 5;
            }
			highlight.chapterFile = current;
			highlight.chapterName = chapterName;
			highlight.percent = percent;
			highlight.text = text;
			highlight.type = annotationType;
			highlight.extra1 = extra1;
			highlight.extra2 = extra2;
			highlight.extra3 = extra3;
			highlight.deviceModel = DeviceInfoUtil.getDeviceModel();
			highlight.osVersion = DeviceInfoUtil.getOSVersion();
			
			hlghHistory.mergeAdd(highlight.uniqueID);
		}
		
		highlightArr.add(highlight);

		result.resultArr = highlightArr;
		result.hlHistory = hlghHistory;
		
    	return result;
    }
    
    /**
     * 주석병합결과 전달용 데이터 객체
     */
    private class CheckHighlightResult{
    	ArrayList<Highlight> resultArr;
    	AnnotationHistory hlHistory;
    }
    
    //2013.12.27 kukang A주석이 B주석을 포함하는지 체크
    private boolean isImplyAnnotation(Highlight a, Highlight b)
    {
        int IndexPosition = 1000000;
        int aStart = (a.startChild * IndexPosition) + a.startChar;
        int aEnd = (a.endChild * IndexPosition) + a.endChar;
        int bStart = (b.startChild * IndexPosition) + b.startChar;
        int bEnd = (b.endChild * IndexPosition) + b.endChar;

        if (aStart <= bStart && aEnd >= bEnd)
        {
            return true;
        }
        return false;
    }

    
    /**
     *  주석데이터 디스플레이용 text 병합처리.
     * 
     */
    private String mergeHighlightText(Highlight a, Highlight b){
    	String text = "";

        int gap = 0;

        
      //2013.12.27 kukang 포함여부체크 수정
        if (isImplyAnnotation(a, b))
        {
            text = a.text;
            return text;
        }
        else if (isImplyAnnotation(b, a))
        {
            text = b.text;
            return text;
        }

        // 2013.12.12 kukang TEXT 병합 - 심정호씨 개발 로직

        //a가 앞에 있는 경우 && a의 끝지점과 b의 시작지점이 같을 경우
        if (a.endChild == b.startChild && 
            (a.startChild < b.startChild || 
            (a.startChild == b.startChild && a.startChar <= b.startChar)))
        {
            gap = a.endChar - b.startChar;
            if (gap >= 0)
            {
                //b가 a에 포함될 때
                if (gap >= b.text.length())
                {
                    text = a.text;
                }
                else
                {
                    text = a.text + b.text.substring(gap);
                }
            }
            //오지 않는 조건문(방어코드)
            else
            {
                text = a.text + b.text;
            }
        }
        //b가 앞에 있는 경우 && b의 끝지점과 a의 시작지점이 같을 경우
        else if (b.endChild == a.startChild &&
            (b.startChild < a.startChild ||
            (b.startChild == a.startChild && b.startChar <= a.startChar)))
        {
            gap = b.endChar - a.startChar;
            if (gap >= 0)
            {
                //a가 b에 포함될 때
                if (gap >= a.text.length())
                {
                    text = b.text;
                }
                else
                {
                    text = b.text + a.text.substring(gap);
                }
            }
            //오지 않는 조건문(방어코드)
            else
            {
                text = b.text + a.text;
            }
        }
        //둘다 같은 문단에 있는 경우
        else if (a.startChild == b.startChild && a.endChild == b.endChild && a.startChild == b.endChild)
        {
            //1이 2보다 뒤에서 시작하는 경우
            if (a.startChar >= b.startChar)
            {
                //1이 2보다 앞에서 끝나는 경우(2에 1이 포함)
                if (a.endChar <= b.endChar)
                {
                    text = b.text;
                }
                //1이 2보다 뒤에서 끝나는 경우
                else
                {
                    gap = b.endChar - a.startChar;
                    if (gap < 0) gap = 0;
                    text = b.text;
                    text += a.text.substring(gap);
                }
            }
            //1이 2보다 앞에서 시작하는 경우
            else
            {
                //1이 2보다 뒤에서 끝나는 경우
                if (b.endChar < a.endChar)
                {
                    text = a.text;
                }
                //1이 2보다 앞에서 끝나는 경우
                else
                {
                    gap = a.endChar - b.startChar;  //2013.12.19 kukang 오타버그 수정
                    if (gap < 0) gap = 0;
                    text = a.text;
                    text += b.text.substring(gap);
                }
            }
        }
        //둘다 같은 문단이 아닌 경우
        else
        {
            String text1 = "";
            String text2 = "";

            //1과 2가 같은 문단에서 시작
            if (a.startChild == b.startChild)
            {
                //1이 2보다 앞에 있는 경우
                if (a.startChar < b.startChar)
                {
                    text1 = a.text;
                    text2 = b.text;
                }
                //2가 1보다 앞에 있는 경우
                else
                {
                    text1 = b.text;
                    text2 = a.text;
                }
            }
            //1이 2보다 앞에서 시작하는 경우
            else if (a.startChild < b.startChild)
            {
                text1 = a.text;
                text2 = b.text;
            }
            //1이 2보다 뒤에서 시작하는 경우
            else
            {
                text1 = b.text;
                text2 = a.text;
            }

            int range = text1.indexOf(text2.substring(0, 3));

            if (range != -1)
            {
                text = text1.substring(0, range);
                text += text2;
            }
            else
            {
                text = text1;
                text += text2;
            }
        }

        return text;
    }
    
    private void highlightsToFile(String fileName, ArrayList<Highlight> highlights){


    	try {
    		if( fileName.length() <= 0 ) return;

    		DebugSet.d(TAG, "highlightsToFile ................ " + fileName);

    		File highlightDataFile = new File(fileName);
    		if( !highlightDataFile.exists()) {
    			highlightDataFile.createNewFile();
    		}

    		FileOutputStream output = new FileOutputStream(highlightDataFile);

    		JSONObject object = new JSONObject();
    		object.put(AnnotationConst.FLK_DATA_TYPE, AnnotationConst.ANNOTATION);
    		object.put(AnnotationConst.FLK_ANNOTATION_VERSION, BookHelper.annotationVersion);

    		JSONArray array = new JSONArray();
    		for(Highlight hilite: highlights) {
    			array.put(hilite.get1());
    		}

    		object.put(AnnotationConst.FLK_ANNOTATION_LIST, array);

    		DebugSet.d(TAG, "json array ................. " + object.toString(1));
    		output.write(object.toString(1).getBytes());
    		output.close();

    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}

    }
    
    private ArrayList<Highlight> highlightsFromFile(String fileName){
    	ArrayList<Highlight> resultArr = new ArrayList<Highlight>();

    	JSONObject object = getJSONObjectFromFile(fileName);

    	if( object == null ) return resultArr;

    	try {

    		DebugSet.d(TAG, "highlightsFromFile ................ " + object.toString(1));

    		JSONArray array = object.getJSONArray(AnnotationConst.FLK_ANNOTATION_LIST);

    		for(int i=0; i<array.length(); i++) {

    			JSONObject jobj = array.getJSONObject(i);

    			long uniqueID; 
    			if( jobj.isNull(AnnotationConst.FLK_ANNOTATION_ID) ) {
    				uniqueID = System.currentTimeMillis() / 1000L;
    			} else {
    				uniqueID = Long.parseLong(jobj.getString(AnnotationConst.FLK_ANNOTATION_ID));
    			}

    			String creationTime = jobj.getString(AnnotationConst.FLK_ANNOTATION_CREATION_TIME);
    			String file = jobj.getString(AnnotationConst.FLK_ANNOTATION_FILE);

    			String startPath = jobj.getString(AnnotationConst.FLK_ANNOTATION_START_ELEMENT_PATH);
    			int startChildIndex = jobj.getInt(AnnotationConst.FLK_ANNOTATION_START_CHILD_INDEX);
    			int startCharOffset = jobj.getInt(AnnotationConst.FLK_ANNOTATION_START_CHAR_OFFSET);
    			String endPath = jobj.getString(AnnotationConst.FLK_ANNOTATION_END_ELEMENT_PATH);
    			int endChildIndex = jobj.getInt(AnnotationConst.FLK_ANNOTATION_END_CHILD_INDEX);
    			int endCharOffset = jobj.getInt(AnnotationConst.FLK_ANNOTATION_END_CHAR_OFFSET);
    			double percent = jobj.getDouble(AnnotationConst.FLK_ANNOTATION_PERCENT);
    			String chapterName = jobj.getString(AnnotationConst.FLK_ANNOTATION_CHAPTER_NAME);
    			String type = jobj.getString(AnnotationConst.FLK_ANNOTATION_TYPE);
    			String text = jobj.getString(AnnotationConst.FLK_ANNOTATION_TEXT);
    			String memo = jobj.getString(AnnotationConst.FLK_ANNOTATION_MEMO);
    			String extra1 = jobj.getString(AnnotationConst.FLK_ANNOTATION_EXTRA1);
    			String extra2 = jobj.getString(AnnotationConst.FLK_ANNOTATION_EXTRA2);
    			String extra3 = jobj.getString(AnnotationConst.FLK_ANNOTATION_EXTRA3);
    			String model = jobj.getString(AnnotationConst.FLK_ANNOTATION_MODEL);
    			String osVersion = jobj.getString(AnnotationConst.FLK_ANNOTATION_OS_VERSION);

    			int colorIndex;
    			if( jobj.isNull(AnnotationConst.FLK_ANNOTATION_COLOR) ) {
    				colorIndex = 0;
    			} else {
    				colorIndex = jobj.getInt(AnnotationConst.FLK_ANNOTATION_COLOR);
    			}

    			Highlight highlight = new Highlight();
    			highlight.highlightID = generateUniqueID();
    			highlight.startPath = startPath;
    			highlight.endPath = endPath;
    			highlight.startChild = startChildIndex;
    			highlight.startChar = startCharOffset;
    			highlight.endChild = endChildIndex;
    			highlight.endChar = endCharOffset;
    			highlight.deleted = false;
    			highlight.annotation = memo.trim().length() > 0 ? true : false;
    			highlight.chapterFile = file;
    			highlight.text = text;
    			highlight.memo = memo;
    			highlight.uniqueID = uniqueID;
    			highlight.colorIndex = colorIndex;
    			highlight.page = -1;
    			highlight.percent = percent;
    			highlight.creationTime = creationTime;
    			highlight.chapterName = chapterName;
    			highlight.type = type;
    			highlight.extra1 = extra1;
    			highlight.extra2 = extra2;
    			highlight.extra3 = extra3;
    			highlight.deviceModel = model;
    			highlight.osVersion = osVersion;

    			resultArr.add(highlight);
    		}

    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}

    	return resultArr;
    }
    
    JSONObject getJSONObjectFromFile(String filePath) {
        
        try {
            File dataFile = new File( filePath );
            if( !dataFile.exists() ) {
                DebugSet.d( TAG, " getJSONObjectFromFile() !DataFile.exists()" );
                return null;
            }
            
            InputStream input = new FileInputStream( dataFile );
           
            String inputByte = BookHelper.inputStream2String(input);
            input.close();
            
            return new JSONObject(inputByte);
               
        } catch( Exception e ) {
            e.printStackTrace();
        }
        
        return null;
    }

    private String generateUniqueID() {
        String randID = "kyoboId" + (int)Math.floor(Math.random()*10001);
        
        return randID;
    }
}

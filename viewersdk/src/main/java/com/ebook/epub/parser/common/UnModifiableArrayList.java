package com.ebook.epub.parser.common;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 수정이 불가능한 ArrayList
 * 
 * @author djHeo
 *
 * @param <T>
 */
public class UnModifiableArrayList<T> {

	private ArrayList<T> arrayList;
	
	public UnModifiableArrayList() {
		arrayList = new ArrayList<T>();
	}
	/**
	 * 일반 ArrayList를 수정이 불가능한 ArrayList로 사용가능하다.
	 * @param array
	 */
	public UnModifiableArrayList(ArrayList<T> array) {
		arrayList = array;
	}
	
	public Iterator<T> iterator(){
		return arrayList.iterator();
	}
	
	public int size(){
		return arrayList.size();
	}
	
	public T get(int i) {
		return arrayList.get(i);
	}
}

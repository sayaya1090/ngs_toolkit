package net.sayaya.ngs.toolkit.data;

public class Pair<A, B> {
	private A a;
	private B b;
	public Pair<A, B> setA(A a) {
		this.a = a;
		return this;
	}
	public Pair<A, B> setB(B b) {
		this.b = b;
		return this;
	}
	public A getA() {
		return a;
	}
	public B getB() {
		return b;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Pair:")
			.append("A=").append(a)
			.append(",B=").append(b)
			.append("]")
		;
		return sb.toString();
	}
}
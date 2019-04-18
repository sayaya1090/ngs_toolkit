package net.sayaya.ngs.toolkit.data;

public class Trio<A, B, C> {
	private A a;
	private B b;
	private C c;
	public Trio<A, B, C> setA(A a) {
		this.a = a;
		return this;
	}
	public Trio<A, B, C> setB(B b) {
		this.b = b;
		return this;
	}
	public Trio<A, B, C> setC(C c) {
		this.c = c;
		return this;
	}
	public A getA() {
		return a;
	}
	public B getB() {
		return b;
	}
	public C getC() {
		return c;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Trio:")
			.append("A=").append(a)
			.append(",B=").append(b)
			.append(",C=").append(c)
			.append("]")
		;
		return sb.toString();
	}
}
package com.thoughtworks.proxy.toys.hotswap;

import com.thoughtworks.proxy.ProxyFactory;
import com.thoughtworks.proxy.ProxyTestCase;
import com.thoughtworks.proxy.factory.CglibProxyFactory;

/**
 * @author Aslak Helles&oslash;y
 * @version $Revision: 1.3 $
 */
public class CglibHotSwappingTest extends ProxyTestCase {
    protected ProxyFactory createProxyFactory() {
        return new CglibProxyFactory();
    }

    public static class Yin {
        private final Yang yang;

        public Yin(Yang yang) {
            this.yang = yang;
        }

        public Yang getYang() {
            return yang;
        }
    }

    public static class Yang {
        private final Yin yin;

        public Yang(Yin yin) {
            this.yin = yin;
        }

        public Yin getYin() {
            return yin;
        }
    }

    public void testShouldMakeMutualDependenciesPossible() {
        Yin yin = (Yin) HotSwapping.object(Yin.class, getFactory(), null);
        Yang yang = new Yang(yin);
        ((Swappable) yin).hotswap(new Yin(yang));

        // isn't this wicked?
        assertSame(yin, yang.getYin());
        assertSame(yang, yin.getYang());
    }

}
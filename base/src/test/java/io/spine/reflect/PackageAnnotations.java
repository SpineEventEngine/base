/*
 * Copyright 2018, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.reflect;

import given.reflect.root.branch1.foo.sub1.Sub1Class;

import java.util.Arrays;

import static java.util.Comparator.comparing;

public class PackageAnnotations {

    private static void printPackages() {
        Package[] pack = Package.getPackages();

        Arrays.sort(pack, comparing(Package::getName));

        System.out.println("Number of packages = " + pack.length);
        // print all packages, one by one
        for (int i = 0; i < pack.length; i++) {
            System.out.println("" + pack[i].getName());
        }
    }

    public static void main(String[] args) {

        // Make sure the class is used.
        Class<Sub1Class> cls = Sub1Class.class;
//        System.out.println("cls.getPackage() = " + cls.getPackage());
//        printPackages();

        Package p = cls.getPackage();

        print("Annotations:");
        print(p.getAnnotations());

        print("Declared annotations:");
        print(p.getDeclaredAnnotations());

        print("Class annotations:");
        print(cls.getAnnotations());
    }

    private static void print(Object... obj) {
        for (Object o : obj) {
            System.out.println(o);
        }
    }
}

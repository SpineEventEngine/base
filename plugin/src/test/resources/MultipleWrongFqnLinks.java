/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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
package io.spine.tools.codestyle.javadoc;
class MultipleWrongFqnLinks {
    private MultipleWrongFqnLinks() {
    }
    /**{@link io.spine.server.event.EventBus}*/
    public static void main(String[] args) {
        System.out.println("");
    }

    /**{@link io.spine.server.event.EventBus    }*/
    public void method(){
        System.out.println("Love yourself");
    }
    /**{@link io.spine.server.event.EventBus    }*/
    public void method1(){
        System.out.println("Take care of your health");
    }
    /**{@link io.spine.server.event.EventBus Event   }*/
    public void method2(){
        System.out.println("Take care of your mind");
    }
    /**{@link io.spine.server.event.EventBus   }*/
    public void method3(){
        System.out.println("Take care of your soul");
    }
}

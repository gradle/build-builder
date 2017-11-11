package org.gradle.builds.assemblers

import spock.lang.Specification

class GraphAssemblerTest extends Specification {
    def assembler = new GraphAssembler()

    def "arranges 1 node"() {
        // <root>
        when:
        def graph = assembler.arrange(1)

        then:
        graph.nodes.size() == 1
        graph.layers.size() == 1

        def root = graph.nodes[0]
        root.dependencies.empty
        root.layer == 0
        root.deepest
        root.nameSuffix == '0Api'
        !root.useAlternate
    }

    def "arranges 2 nodes"() {
        // <root> -> <node>
        when:
        def graph = assembler.arrange(2)

        then:
        def nodes = graph.nodes
        graph.nodes.size() == 2
        graph.layers.size() == 2

        def root = nodes[0]
        def n1 = nodes[1]

        root.dependencies == [n1]
        root.layer == 0
        !root.deepest
        root.nameSuffix == '0Api'
        !root.useAlternate

        n1.dependencies.empty
        n1.layer == 1
        n1.deepest
        n1.nameSuffix == '1Api'
        n1.useAlternate
    }

    def "arranges 3 nodes"() {
        // <root> -> <node1> -> <node2>
        when:
        def graph = assembler.arrange(3)

        then:
        def nodes = graph.nodes
        graph.nodes.size() == 3
        graph.layers.size() == 3

        def root = nodes[0]
        def n1 = nodes[1]
        def n2 = nodes[2]

        root.dependencies == [n1]
        root.layer == 0
        !root.deepest
        root.nameSuffix == '0Api'
        !root.useAlternate

        n1.dependencies == [n2]
        n1.layer == 1
        !n1.deepest
        n1.nameSuffix == '1Api'
        !n1.useAlternate

        n2.dependencies.empty
        n2.layer == 2
        n2.deepest
        n2.nameSuffix == '2Api'
        n2.useAlternate
    }

    def "arranges 4 nodes"() {
        // <root> -> <node1> -> <node3>
        //        -> <node2> ->
        when:
        def graph = assembler.arrange(4)

        then:
        def nodes = graph.nodes as List
        graph.nodes.size() == 4
        graph.layers.size() == 3

        def root = nodes[0]
        def n1 = nodes[1]
        def n2 = nodes[2]
        def n3 = nodes[3]

        root.dependencies == [n1, n2]
        root.layer == 0
        !root.deepest
        root.nameSuffix == '0Api'
        !root.useAlternate

        n1.dependencies == [n3]
        n1.layer == 1
        !n1.deepest
        n1.nameSuffix == '1Api1'
        !n1.useAlternate

        n2.dependencies == [n3]
        n2.layer == 1
        !n2.deepest
        n2.nameSuffix == '1Api2'
        n2.useAlternate

        n3.dependencies.empty
        n3.layer == 2
        n3.deepest
        n3.nameSuffix == '2Api'
        n3.useAlternate
    }

    def "arranges 5 nodes"() {
        // <root> -> <node1> ->          <node4>
        //        -> <node2> -> <node3>
        when:
        def graph = assembler.arrange(5)

        then:
        def nodes = graph.nodes
        graph.nodes.size() == 5
        graph.layers.size() == 3

        def root = nodes[0]
        def n1 = nodes[1]
        def n2 = nodes[2]
        def n3 = nodes[3]
        def n4 = nodes[4]

        root.dependencies == [n1, n2]
        root.layer == 0
        root.nameSuffix == '0Api'
        !root.useAlternate

        n1.dependencies == [n3, n4]
        n1.layer == 1
        !n1.deepest
        n1.nameSuffix == '1Api1'
        !n1.useAlternate

        n2.dependencies == [n3, n4]
        n2.layer == 1
        !n2.deepest
        n2.nameSuffix == '1Api2'
        n2.useAlternate

        n3.dependencies.empty
        n3.layer == 1
        !n3.deepest
        n3.nameSuffix == '1Core'
        n3.useAlternate

        n4.dependencies.empty
        n4.layer == 2
        n4.deepest
        n4.nameSuffix == '2Api'
        n4.useAlternate
    }

    def "arranges 6 nodes"() {
        // <root> -> <node1> ->          <node4>
        //        -> <node2> ->          <node5>
        //                   -> <node3>
        when:
        def graph = assembler.arrange(6)

        then:
        def nodes = graph.nodes as List
        graph.nodes.size() == 6
        graph.layers.size() == 3

        def root = nodes[0]
        def n1 = nodes[1]
        def n2 = nodes[2]
        def n3 = nodes[3]
        def n4 = nodes[4]
        def n5 = nodes[5]

        root.dependencies == [n1, n2]
        !root.deepest
        root.nameSuffix == '0Api'
        !root.useAlternate

        n1.dependencies == [n3, n4, n5]
        !n1.deepest
        n1.nameSuffix == '1Api1'
        !n1.useAlternate

        n2.dependencies == [n3, n4, n5]
        !n2.deepest
        n2.nameSuffix == '1Api2'
        !n2.useAlternate

        n3.dependencies.empty
        !n3.deepest
        n3.nameSuffix == '1Core'
        n3.useAlternate

        n4.dependencies.empty
        !n4.deepest
        n4.nameSuffix == '2Api1'
        !n4.useAlternate

        n5.dependencies.empty
        n5.deepest
        n5.nameSuffix == '2Api2'
        n5.useAlternate
    }

    def "arranges 7 nodes"() {
        // <root> -> <node1> ->                    <node5>
        //        -> <node2> ->                    <node6>
        //                   -> <node3> ->
        //                              -> <node4>
        when:
        def graph = assembler.arrange(7)

        then:
        def nodes = graph.nodes as List
        graph.nodes.size() == 7
        graph.layers.size() == 3

        def root = nodes[0]
        def n1 = nodes[1]
        def n2 = nodes[2]
        def n3 = nodes[3]
        def n4 = nodes[4]
        def n5 = nodes[5]
        def n6 = nodes[6]

        root.dependencies == [n1, n2]
        !root.deepest
        root.nameSuffix == '0Api'
        !root.useAlternate

        n1.dependencies == [n3, n5, n6]
        !n1.deepest
        n1.nameSuffix == '1Api1'
        !n1.useAlternate

        n2.dependencies == [n3, n5, n6]
        !n2.deepest
        n2.nameSuffix == '1Api2'
        !n2.useAlternate

        n3.dependencies == [n4, n5, n6]
        !n3.deepest
        n3.nameSuffix == '1Impl'
        !n3.useAlternate

        n4.dependencies.empty
        !n4.deepest
        n4.nameSuffix == '1Core'
        n4.useAlternate

        n5.dependencies.empty
        !n5.deepest
        n5.nameSuffix == '2Api1'
        !n5.useAlternate

        n6.dependencies.empty
        n6.deepest
        n6.nameSuffix == '2Api2'
        n6.useAlternate
    }

    def "arranges 8 nodes"() {
        // <root> -> <node1> ->                    <node6>
        //        -> <node2> ->                    <node7>
        //        -> <node3> ->
        //                   -> <node4> ->
        //                              -> <node5>
        when:
        def graph = assembler.arrange(8)

        then:
        def nodes = graph.nodes as List
        graph.nodes.size() == 8
        graph.layers.size() == 3

        def root = nodes[0]
        def n1 = nodes[1]
        def n2 = nodes[2]
        def n3 = nodes[3]
        def n4 = nodes[4]
        def n5 = nodes[5]
        def n6 = nodes[6]
        def n7 = nodes[7]

        root.dependencies == [n1, n2, n3]
        !root.deepest
        root.nameSuffix == '0Api'

        n1.dependencies == [n4, n6, n7]
        !n1.deepest
        n1.nameSuffix == '1Api1'

        n2.dependencies == [n4, n6, n7]
        !n2.deepest
        n2.nameSuffix == '1Api2'

        n3.dependencies == [n4, n6, n7]
        !n3.deepest
        n3.nameSuffix == '1Api3'

        n4.dependencies == [n5, n6, n7]
        !n4.deepest
        n4.nameSuffix == '1Impl'

        n5.dependencies.empty
        !n5.deepest
        n5.nameSuffix == '1Core'

        n6.dependencies.empty
        !n6.deepest
        n6.nameSuffix == '2Api1'

        n7.dependencies.empty
        n7.deepest
        n7.nameSuffix == '2Api2'
    }

    def "arranges 9 nodes"() {
        // <root> -> <node1> ->                    <node7>
        //        -> <node2> ->                    <node8>
        //        -> <node3> ->
        //                   -> <node4> ->
        //                              -> <node5>
        //                              -> <node6>
        when:
        def graph = assembler.arrange(9)

        then:
        def nodes = graph.nodes as List
        graph.nodes.size() == 9
        graph.layers.size() == 3

        nodes[0].dependencies == [nodes[1], nodes[2], nodes[3]]
        nodes[1].dependencies == [nodes[4], nodes[7], nodes[8]]
        nodes[2].dependencies == [nodes[4], nodes[7], nodes[8]]
        nodes[3].dependencies == [nodes[4], nodes[7], nodes[8]]
        nodes[4].dependencies == [nodes[5], nodes[6], nodes[7], nodes[8]]
        nodes[5].dependencies.empty
        nodes[5].nameSuffix == '1Core1'
        nodes[6].dependencies.empty
        nodes[6].nameSuffix == '1Core2'
        nodes[7].dependencies.empty
        nodes[8].dependencies.empty
    }

    def "arranges 10 nodes"() {
        // <root> -> <node1> ->                    <node7> -> <node9>
        //        -> <node2> ->                    <node8> ->
        //        -> <node3> ->
        //                   -> <node4> ->
        //                              -> <node5>
        //                              -> <node6>
        when:
        def graph = assembler.arrange(10)

        then:
        def nodes = graph.nodes as List
        graph.nodes.size() == 10
        graph.layers.size() == 4

        nodes[0].dependencies == [nodes[1], nodes[2], nodes[3]]
        nodes[1].dependencies == [nodes[4], nodes[7], nodes[8]]
        nodes[2].dependencies == [nodes[4], nodes[7], nodes[8]]
        nodes[3].dependencies == [nodes[4], nodes[7], nodes[8]]
        nodes[4].dependencies == [nodes[5], nodes[6], nodes[7], nodes[8]]
        nodes[5].dependencies.empty
        nodes[6].dependencies.empty
        nodes[7].dependencies == [nodes[9]]
        nodes[8].dependencies == [nodes[9]]
        nodes[9].dependencies.empty
    }

    def "arranges 11 nodes"() {
        // <root> -> <node1> ->                    <node7> ->         <node10>
        //        -> <node2> ->                    <node8> ->
        //        -> <node3> ->                            -> <node9>
        //                   -> <node4> ->
        //                              -> <node5>
        //                              -> <node6>
        when:
        def graph = assembler.arrange(11)

        then:
        def nodes = graph.nodes as List
        graph.nodes.size() == 11
        graph.layers.size() == 4

        nodes[0].dependencies == [nodes[1], nodes[2], nodes[3]]
        !nodes[0].useAlternate
        nodes[1].dependencies == [nodes[4], nodes[7], nodes[8]]
        !nodes[1].useAlternate
        nodes[2].dependencies == [nodes[4], nodes[7], nodes[8]]
        !nodes[2].useAlternate
        nodes[3].dependencies == [nodes[4], nodes[7], nodes[8]]
        !nodes[3].useAlternate
        nodes[4].dependencies == [nodes[5], nodes[6], nodes[7], nodes[8]]
        !nodes[4].useAlternate
        nodes[5].dependencies.empty
        !nodes[5].useAlternate
        nodes[6].dependencies.empty
        nodes[6].useAlternate
        nodes[7].dependencies == [nodes[9], nodes[10]]
        !nodes[7].useAlternate
        nodes[8].dependencies == [nodes[9], nodes[10]]
        nodes[8].useAlternate
        nodes[9].dependencies.empty
        nodes[9].useAlternate
        nodes[10].dependencies.empty
        nodes[10].useAlternate
    }

    def "arranges 12 nodes"() {
        // <root> -> <node1> ->                    <node7> ->         <node10>
        //        -> <node2> ->                    <node8> ->         <node11>
        //        -> <node3> ->                            -> <node9>
        //                   -> <node4> ->
        //                              -> <node5>
        //                              -> <node6>
        when:
        def graph = assembler.arrange(12)

        then:
        def nodes = graph.nodes as List
        graph.nodes.size() == 12
        graph.layers.size() == 4

        nodes[0].dependencies == [nodes[1], nodes[2], nodes[3]]
        nodes[1].dependencies == [nodes[4], nodes[7], nodes[8]]
        nodes[2].dependencies == [nodes[4], nodes[7], nodes[8]]
        nodes[3].dependencies == [nodes[4], nodes[7], nodes[8]]
        nodes[4].dependencies == [nodes[5], nodes[6], nodes[7], nodes[8]]
        nodes[5].dependencies.empty
        nodes[5].nameSuffix == '1Core1'
        nodes[6].dependencies.empty
        nodes[6].nameSuffix == '1Core2'
        nodes[7].dependencies == [nodes[9], nodes[10], nodes[11]]
        nodes[8].dependencies == [nodes[9], nodes[10], nodes[11]]
        nodes[9].dependencies.empty
        nodes[9].nameSuffix == '2Core'
        nodes[10].dependencies.empty
        nodes[11].dependencies.empty
    }

    def "arranges 17 nodes"() {
        // <root> -> <node1> ->                      <node7> ->                      <node13> ->           <node16>
        //        -> <node2> ->                      <node8> ->                      <node14> ->
        //        -> <node3> ->                      <node9> ->                               -> <node15>
        //                   -> <node4> ->                   -> <node10> ->
        //                              -> <node5>                       -> <node11>
        //                              -> <node6>                       -> <node12>
        when:
        def graph = assembler.arrange(17)

        then:
        def nodes = graph.nodes as List
        graph.nodes.size() == 17
        graph.layers.size() == 5

        nodes[0].dependencies == [nodes[1], nodes[2], nodes[3]]
        nodes[1].dependencies == [nodes[4], nodes[7], nodes[8], nodes[9]]
        nodes[2].dependencies == [nodes[4], nodes[7], nodes[8], nodes[9]]
        nodes[3].dependencies == [nodes[4], nodes[7], nodes[8], nodes[9]]
        nodes[4].dependencies == [nodes[5], nodes[6], nodes[7], nodes[8], nodes[9]]
        nodes[5].dependencies.empty
        nodes[6].dependencies.empty
        nodes[7].dependencies == [nodes[10], nodes[13], nodes[14]]
        nodes[8].dependencies == [nodes[10], nodes[13], nodes[14]]
        nodes[9].dependencies == [nodes[10], nodes[13], nodes[14]]
        nodes[10].dependencies == [nodes[11], nodes[12], nodes[13], nodes[14]]
        nodes[11].dependencies.empty
        nodes[12].dependencies.empty
        nodes[13].dependencies == [nodes[15], nodes[16]]
        nodes[14].dependencies == [nodes[15], nodes[16]]
        nodes[15].dependencies.empty
        nodes[16].dependencies.empty
        nodes[16].deepest
    }
}

/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package activityconfig.snakecharmer;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is as arcane and strange looking as it sounds. SnakeYaml has a design that does not
 * make it easy to mix and match bean-style constructions with generic collections or variant
 * YAML structure (as allowed by the YAML spec), so this is a set of surgical APIs that
 * help to do such.
 *
 * It would be a nice improvement to be able to drop these classes and rely instead directly on
 * SnakeYaml APIs of similar flavor if/when it can do so, or if/when the documentation explains
 * more cleanly how to do so.
 */
public class SnakeYamlCharmer extends Constructor {
    private final Map<String,HandlerDef> handlerDefs = new HashMap<>();
    private final ReSafeConstructor ctor = new ReSafeConstructor();

    public SnakeYamlCharmer(Class<?> targetClass) {
        super(targetClass);
        this.yamlClassConstructors.put(NodeId.mapping, new DelegatorConstructor());
    }

    public <T> void addHandler(Class<T> typeName, String fieldName, FieldHandler fieldHandler) {
        this.handlerDefs.put(fieldName, new HandlerDef(fieldName, typeName, fieldHandler));
    }

    public interface FieldHandler {
        void handleMapping(Object object, Object subObject);
    }

    private static class HandlerDef {
        final String fieldName;
        final Class<?> handlerClass;
        final FieldHandler handler;

        HandlerDef(String fieldName, Class<?> handlerClass, FieldHandler handler) {
            this.fieldName = fieldName;
            this.handlerClass = handlerClass;
            this.handler = handler;
        }
    }

    private class DelegatorConstructor extends Constructor.ConstructMapping {

        @Override
        protected Object constructJavaBean2ndStep(MappingNode node, Object object) {

            if (node.getNodeId()==NodeId.mapping) {
                List<NodeTuple> toExtract = new ArrayList<>();

                // Find all matching field names and remember them
                for (NodeTuple nodeTuple : node.getValue()) {
                    Node prospectNode = nodeTuple.getKeyNode();
                    if (nodeTuple.getKeyNode() instanceof ScalarNode) {
                        ScalarNode nameNode = (ScalarNode) prospectNode;
                        if (SnakeYamlCharmer.this.handlerDefs.keySet().contains(nameNode.getValue())) {
                            toExtract.add(nodeTuple);
                        }
                    }
                }

                /**
                 * Remove each matching field name by node and owning object
                 * Construct a safe collection-based Java object
                 * Call the delegated handler for the owning object and the sub-object
                 */
                for (NodeTuple nodeTuple : toExtract) {
                    node.getValue().remove(nodeTuple);
                    String nodeName = ((ScalarNode) nodeTuple.getKeyNode()).getValue();
                    Object subObject = ctor.constructObject(nodeTuple.getValueNode());
                    HandlerDef handlerDef = SnakeYamlCharmer.this.handlerDefs.get(nodeName);
                    handlerDef.handler.handleMapping(object,subObject);
                }
            }
            return super.constructJavaBean2ndStep(node,object);
        }
    }

    public static class ReSafeConstructor extends SafeConstructor {
        @Override
        public Object constructObject(Node node) {
            return super.constructObject(node);
        }
    }
}

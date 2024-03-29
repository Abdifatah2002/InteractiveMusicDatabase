// --== CS400 Spring 2023 File Header Information ==--
// Name: Matthew Wang
// Email: mewang@wisc.edu
// Team: AN Blue
// TA: Formerly LAIK RUETTEN, now Gary Dahl
// Lecturer: Gary Dahl
// Notes to Grader: N/A

import java.util.LinkedList;
import java.util.Stack;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Red-Black Tree implementation with a Node inner class for representing
 * the nodes of the tree. Currently, this implements a Binary Search Tree that
 * we will turn into a red black tree by modifying the insert functionality.
 * In this activity, we will start with implementing rotations for the binary
 * search tree insert algorithm.
 */
public class RedBlackTreeAE<T extends Comparable<T>> implements SortedCollectionInterface<T> {

    /**
     * This class represents a node holding a single value within a binary tree.
     */
    protected static class Node<T> {
        public T data;
        public int blackHeight;
        // The context array stores the context of the node in the tree:
        // - context[0] is the parent reference of the node,
        // - context[1] is the left child reference of the node,
        // - context[2] is the right child reference of the node.
        // The @SupressWarning("unchecked") annotation is used to supress an unchecked
        // cast warning. Java only allows us to instantiate arrays without generic
        // type parameters, so we use this cast here to avoid future casts of the
        // node type's data field.
        @SuppressWarnings("unchecked")
        public Node<T>[] context = (Node<T>[]) new Node[3];

        public Node(T data) {
            this.data = data;
            blackHeight = 0;
        }

        /**
         * @return true when this node has a parent and is the right child of
         * that parent, otherwise return false
         */
        public boolean isRightChild() {
            return context[0] != null && context[0].context[2] == this;
        }

    }

    protected Node<T> root; // reference to root node of tree, null when empty
    protected int size = 0; // the number of values in the tree

    /**
     * Performs a naive insertion into a binary search tree: adding the input
     * data value to a new node in a leaf position within the tree. After
     * this insertion, no attempt is made to restructure or balance the tree.
     * This tree will not hold null references, nor duplicate data values.
     *
     * @param data to be added into this binary search tree
     * @return true if the value was inserted, false if not
     * @throws NullPointerException     when the provided data argument is null
     * @throws IllegalArgumentException when data is already contained in the tree
     */
    public boolean insert(T data) throws NullPointerException, IllegalArgumentException {
        // null references cannot be stored within this tree
        if (data == null) throw new NullPointerException(
                "This RedBlackTree cannot store null references.");

        Node<T> newNode = new Node<>(data);
        if (this.root == null) {
            // add first node to an empty tree
            root = newNode;
            size++;
            enforceRBTreePropertiesAfterInsert(newNode);
            return true;
        } else {
            // insert into subtree
            Node<T> current = this.root;
            while (true) {
                int compare = newNode.data.compareTo(current.data);
                if (compare == 0) {
                    throw new IllegalArgumentException("This RedBlackTree already contains value " + data.toString());
                } else if (compare < 0) {
                    // insert in left subtree
                    if (current.context[1] == null) {
                        // empty space to insert into
                        current.context[1] = newNode;
                        newNode.context[0] = current;
                        this.size++;
                        enforceRBTreePropertiesAfterInsert(newNode);
                        return true;
                    } else {
                        // no empty space, keep moving down the tree
                        current = current.context[1];
                    }
                } else {
                    // insert in right subtree
                    if (current.context[2] == null) {
                        // empty space to insert into
                        current.context[2] = newNode;
                        newNode.context[0] = current;
                        this.size++;
                        enforceRBTreePropertiesAfterInsert(newNode);
                        return true;
                    } else {
                        // no empty space, keep moving down the tree
                        current = current.context[2];
                    }
                }
            }
        }
    }

    /**
     * This method resolves any red-black tree property violations that are
     * introduced by inserting each new node into a red-black tree.
     *
     * @param checkNode, the node added / causing a violation in the tree
     */
    protected void enforceRBTreePropertiesAfterInsert(Node<T> checkNode) {

        // instance variables
        Node<T> grandparent;
        Node<T> parent;
        Node<T> uncle = null;

        // setting the parent of node being checked
        if (checkNode.context[0] != null) {
            parent = checkNode.context[0];

            // if the parent is black with a red child, there is no violation
            if (parent.blackHeight == 1) {
                return;
            }

            // setting the grandparent
            if (parent.context[0] != null) {
                grandparent = parent.context[0];

                // setting the uncle (if the uncle is on the left)
                if (parent.isRightChild()) {
                    if (grandparent.context[1] != null) {
                        uncle = grandparent.context[1];
                    }
                } else {
                    // setting the uncle (if the uncle is on the right)
                    if (grandparent.context[2] != null) {
                        uncle = grandparent.context[2];
                    }
                }

                if (checkNode.blackHeight == 0 && checkNode.context[0].blackHeight == 0) {
                    if ((uncle == null || uncle.blackHeight == 1)) {

                        // check if its case 1 (uncle is black with parent and child on same side)
                        if ((checkNode.isRightChild() && parent.isRightChild()) ||
                                (!checkNode.isRightChild() && !parent.isRightChild())) {

                            case1(checkNode);
                        }

                        // check if its case 2
                        else if ((checkNode.isRightChild() && !parent.isRightChild()) ||
                                (!checkNode.isRightChild() && parent.isRightChild())) {
                            case2(checkNode);
                        }
                    }

                    // check if its case 3
                    else {
                        case3(checkNode, uncle);
                    }
                }
            }

        }
        root.blackHeight = 1;
    }

    /**
     * Performs (does not check) case 1 as discussed in class - when the parent of the
     * inserted node's sibling is black and the parent and child are on the same side,
     * rotate and color swap
     *
     * @param checkNode, the node added / causing a violation in the tree
     */
    protected void case1(Node<T> checkNode) {
        checkNode.context[0].context[0].blackHeight = 0;
        checkNode.context[0].blackHeight = 1;

        rotate(checkNode.context[0], checkNode.context[0].context[0]);
    }

    /**
     * Performs (does not check) case 2 as discussed in class - when the parent of
     * the inserted node's sibling is black and the parent and child are NOT on the
     * same side, rotate the 2 red nodes and then do case 1
     *
     * @param checkNode, the node added / causing a violation in the tree
     */
    protected void case2(Node<T> checkNode) {
        rotate(checkNode, checkNode.context[0]);

        checkNode.context[0].blackHeight = 0;
        checkNode.blackHeight = 1;

        rotate(checkNode, checkNode.context[0]);
    }

    /**
     * Performs (does not check) case 3 as discussed in class - when the parent of
     * the inserted node's sibling is red, change the color of the parent, parent's sibling,
     * and parent's parent
     *
     * @param checkNode, the node added / causing a violation in the tree
     */
    protected void case3(Node<T> checkNode, Node<T> uncle) {

        // changing the parent's color
        checkNode.context[0].blackHeight = 1;

        // changing the uncle
        uncle.blackHeight = 1;

        // changing the grandparent
        checkNode.context[0].context[0].blackHeight = 0;

        // recursion, no need for base case because of the conditionals within the method
        enforceRBTreePropertiesAfterInsert(checkNode.context[0].context[0]);

    }

    /**
     * Performs the rotation operation on the provided nodes within this tree.
     * When the provided child is a left child of the provided parent, this
     * method will perform a right rotation. When the provided child is a
     * right child of the provided parent, this method will perform a left rotation.
     * When the provided nodes are not related in one of these ways, this method
     * will throw an IllegalArgumentException.
     *
     * @param child  is the node being rotated from child to parent position
     *               (between these two node arguments)
     * @param parent is the node being rotated from parent to child position
     *               (between these two node arguments)
     * @throws IllegalArgumentException when the provided child and parent
     *                                  node references are not initially (pre-rotation) related that way
     */
    protected void rotate(Node<T> child, Node<T> parent) throws IllegalArgumentException {
        // exception handling and making sure that if the child node isn't the right child, it will be the left child
        if (child.context[0] == null || !child.context[0].equals(parent))
            throw new IllegalArgumentException("Error: the child and parent node are not related.");

        int top = 0;
        int left = 1;
        int right = 2;

        // LEFT ROTATION
        if (child.isRightChild()) {
            // we connect the child's left node with the parents right node (previously the child itself)
            parent.context[right] = child.context[left];
            if (child.context[left] != null) {
                child.context[left].context[top] = parent;
            }
            // if the parent is the root of the bst and not in the middle, we do not have to worry about the grandparents
            if (parent.equals(root)) {
                root = child;

            } else {
                if (parent.isRightChild()) {
                    // if the parent is a right child, we set the grandparents right node into the child
                    parent.context[top].context[right] = child;
                    child.context[top] = parent.context[top];
                } else {
                    // if the parent is a left child, we set the grandparents right node into the child
                    parent.context[top].context[left] = child;
                    child.context[top] = parent.context[top];
                }
            }
            child.context[left] = parent; // we connect the child with the parent, now with the
            parent.context[top] = child; // parent as the child and the child as the parent
        }


        // RIGHT ROTATION
        else {
            // we connect the child's left node with the parents right node (previously the child itself)
            parent.context[left] = child.context[right];
            if (child.context[right] != null) {
                child.context[right].context[top] = parent;
            }

            // if the parent is the root of the bst and not in the middle, we do not have to worry about the grandparents
            if (parent.equals(root)) {
                root = child;
            } else {
                if (parent.isRightChild()) {
                    // if the parent is a right child, we set the grandparents right node into the child
                    parent.context[top].context[right] = child;
                    child.context[top] = parent.context[top];
                } else {
                    // if the parent is a left child, we set the grandparents left node into the child
                    parent.context[top].context[left] = child;
                    child.context[top] = parent.context[top];
                }
            }

            child.context[right] = parent; // we connect the child with the parent, now with the
            parent.context[top] = child; // parent as the child and the child as the parent
        }
    }

    /**
     * Get the size of the tree (its number of nodes).
     *
     * @return the number of nodes in the tree
     */
    public int size() {
        return size;
    }

    /**
     * Method to check if the tree is empty (does not contain any node).
     *
     * @return true of this.size() return 0, false if this.size() > 0
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Removes the value data from the tree if the tree contains the value.
     * This method will not attempt to rebalance the tree after the removal and
     * should be updated once the tree uses Red-Black Tree insertion.
     *
     * @return true if the value was remove, false if it didn't exist
     * @throws NullPointerException     when the provided data argument is null
     * @throws IllegalArgumentException when data is not stored in the tree
     */
    public boolean remove(T data) throws NullPointerException, IllegalArgumentException {
        // null references will not be stored within this tree
        if (data == null) {
            throw new NullPointerException("This RedBlackTree cannot store null references.");
        } else {
            Node<T> nodeWithData = this.findNodeWithData(data);
            // throw exception if node with data does not exist
            if (nodeWithData == null) {
                throw new IllegalArgumentException("The following value is not in the tree and cannot be deleted: " + data.toString());
            }
            boolean hasRightChild = (nodeWithData.context[2] != null);
            boolean hasLeftChild = (nodeWithData.context[1] != null);
            if (hasRightChild && hasLeftChild) {
                // has 2 children
                Node<T> successorNode = this.findMinOfRightSubtree(nodeWithData);
                // replace value of node with value of successor node
                nodeWithData.data = successorNode.data;
                // remove successor node
                if (successorNode.context[2] == null) {
                    // successor has no children, replace with null
                    this.replaceNode(successorNode, null);
                } else {
                    // successor has a right child, replace successor with its child
                    this.replaceNode(successorNode, successorNode.context[2]);
                }
            } else if (hasRightChild) {
                // only right child, replace with right child
                this.replaceNode(nodeWithData, nodeWithData.context[2]);
            } else if (hasLeftChild) {
                // only left child, replace with left child
                this.replaceNode(nodeWithData, nodeWithData.context[1]);
            } else {
                // no children, replace node with a null node
                this.replaceNode(nodeWithData, null);
            }
            this.size--;
            return true;
        }
    }

    /**
     * Checks whether the tree contains the value *data*.
     *
     * @param data the data value to test for
     * @return true if *data* is in the tree, false if it is not in the tree
     */
    public boolean contains(T data) {
        // null references will not be stored within this tree
        if (data == null) {
            throw new NullPointerException("This RedBlackTree cannot store null references.");
        } else {
            Node<T> nodeWithData = this.findNodeWithData(data);
            // return false if the node is null, true otherwise
            return (nodeWithData != null);
        }
    }

    /**
     * Helper method that will replace a node with a replacement node. The replacement
     * node may be null to remove the node from the tree.
     *
     * @param nodeToReplace   the node to replace
     * @param replacementNode the replacement for the node (may be null)
     */
    protected void replaceNode(Node<T> nodeToReplace, Node<T> replacementNode) {
        if (nodeToReplace == null) {
            throw new NullPointerException("Cannot replace null node.");
        }
        if (nodeToReplace.context[0] == null) {
            // we are replacing the root
            if (replacementNode != null)
                replacementNode.context[0] = null;
            this.root = replacementNode;
        } else {
            // set the parent of the replacement node
            if (replacementNode != null)
                replacementNode.context[0] = nodeToReplace.context[0];
            // do we have to attach a new left or right child to our parent?
            if (nodeToReplace.isRightChild()) {
                nodeToReplace.context[0].context[2] = replacementNode;
            } else {
                nodeToReplace.context[0].context[1] = replacementNode;
            }
        }
    }

    /**
     * Helper method that will return the inorder successor of a node with two children.
     *
     * @param node the node to find the successor for
     * @return the node that is the inorder successor of node
     */
    protected Node<T> findMinOfRightSubtree(Node<T> node) {
        if (node.context[1] == null && node.context[2] == null) {
            throw new IllegalArgumentException("Node must have two children");
        }
        // take a steop to the right
        Node<T> current = node.context[2];
        while (true) {
            // then go left as often as possible to find the successor
            if (current.context[1] == null) {
                // we found the successor
                return current;
            } else {
                current = current.context[1];
            }
        }
    }

    /**
     * Helper method that will return the node in the tree that contains a specific
     * value. Returns null if there is no node that contains the value.
     *
     * @return the node that contains the data, or null of no such node exists
     */
    protected Node<T> findNodeWithData(T data) {
        Node<T> current = this.root;
        while (current != null) {
            int compare = data.compareTo(current.data);
            if (compare == 0) {
                // we found our value
                return current;
            } else if (compare < 0) {
                // keep looking in the left subtree
                current = current.context[1];
            } else {
                // keep looking in the right subtree
                current = current.context[2];
            }
        }
        // we're at a null node and did not find data, so it's not in the tree
        return null;
    }

    /**
     * This method performs an inorder traversal of the tree. The string
     * representations of each data value within this tree are assembled into a
     * comma separated string within brackets (similar to many implementations
     * of java.util.Collection, like java.util.ArrayList, LinkedList, etc).
     *
     * @return string containing the ordered values of this tree (in-order traversal)
     */
    public String toInOrderString() {
        // generate a string of all values of the tree in (ordered) in-order
        // traversal sequence
        StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        if (this.root != null) {
            Stack<Node<T>> nodeStack = new Stack<>();
            Node<T> current = this.root;
            while (!nodeStack.isEmpty() || current != null) {
                if (current == null) {
                    Node<T> popped = nodeStack.pop();
                    sb.append(popped.data.toString());
                    if (!nodeStack.isEmpty() || popped.context[2] != null) sb.append(", ");
                    current = popped.context[2];
                } else {
                    nodeStack.add(current);
                    current = current.context[1];
                }
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * This method performs a level order traversal of the tree. The string
     * representations of each data value
     * within this tree are assembled into a comma separated string within
     * brackets (similar to many implementations of java.util.Collection).
     * This method will be helpful as a helper for the debugging and testing
     * of your rotation implementation.
     *
     * @return string containing the values of this tree in level order
     */
    public String toLevelOrderString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        if (this.root != null) {
            LinkedList<Node<T>> q = new LinkedList<>();
            q.add(this.root);
            while (!q.isEmpty()) {
                Node<T> next = q.removeFirst();
                if (next.context[1] != null) q.add(next.context[1]);
                if (next.context[2] != null) q.add(next.context[2]);
                sb.append(next.data.toString());
                if (!q.isEmpty()) sb.append(", ");
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * This method performs a level order traversal of the tree. The string
     * representations of each node's color (0 for red, 1 for black, 2 for double black)
     * within this tree are assembled into a comma separated string within
     * brackets (similar to many implementations of java.util.Collection).
     * This method will be helpful as a helper for the debugging and testing
     * of your rotation implementation.
     *
     * @return string containing the values of colors in tree in level order
     */
    protected String toLevelOrderColor() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        if (this.root != null) {
            LinkedList<Node<T>> q = new LinkedList<>();
            q.add(this.root);
            while (!q.isEmpty()) {
                Node<T> next = q.removeFirst();
                if (next.context[1] != null) q.add(next.context[1]);
                if (next.context[2] != null) q.add(next.context[2]);
                sb.append(next.blackHeight);
                if (!q.isEmpty()) sb.append(", ");
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    public String toString() {
        return "level order: " + this.toLevelOrderString() +
                "\nin order: " + this.toInOrderString();
    }


    /**
     * This method checks case 1 functionality through JUnit testing. When the parent of the
     * inserted node's sibling is black and the parent and child are on the same side,
     * rotate and color swap
     */
    @Test
    public void test1() {

        RedBlackTreeAE<Integer> test = new RedBlackTreeAE<>();

        test.insert(20);
        test.insert(10);
        test.insert(30);
        test.insert(5);
        test.insert(15);
        test.insert(25);
        test.insert(35);

        assertEquals("[ 20, 10, 30, 5, 15, 25, 35 ]", test.toLevelOrderString());
        assertEquals("[ 1, 1, 1, 0, 0, 0, 0 ]", test.toLevelOrderColor());

        test.insert(33);

        assertEquals("[ 20, 10, 30, 5, 15, 25, 35, 33 ]", test.toLevelOrderString());
        assertEquals("[ 1, 1, 0, 0, 0, 1, 1, 0 ]", test.toLevelOrderColor());

        test.insert(32);

        assertEquals("[ 20, 10, 30, 5, 15, 25, 33, 32, 35 ]", test.toLevelOrderString());
        assertEquals("[ 1, 1, 0, 0, 0, 1, 1, 0, 0 ]", test.toLevelOrderColor());

    }

    /**
     * This method checks case 2 functionality through JUnit testing. When the parent of
     * the inserted node's sibling is black and the parent and child are NOT on the
     * same side, rotate the 2 red nodes and then do case 1
     */
    @Test
    public void test2() {

        RedBlackTreeAE<Integer> test = new RedBlackTreeAE<>();

        test.insert(20);
        test.insert(10);
        test.insert(30);
        test.insert(5);
        test.insert(15);
        test.insert(25);
        test.insert(35);


        assertEquals("[ 20, 10, 30, 5, 15, 25, 35 ]", test.toLevelOrderString());
        assertEquals("[ 1, 1, 1, 0, 0, 0, 0 ]", test.toLevelOrderColor());

        test.insert(33);

        assertEquals("[ 20, 10, 30, 5, 15, 25, 35, 33 ]", test.toLevelOrderString());
        assertEquals("[ 1, 1, 0, 0, 0, 1, 1, 0 ]", test.toLevelOrderColor());

        test.insert(34);

        assertEquals("[ 20, 10, 30, 5, 15, 25, 34, 33, 35 ]", test.toLevelOrderString());
        assertEquals("[ 1, 1, 0, 0, 0, 1, 1, 0, 0 ]", test.toLevelOrderColor());

        test.insert(3);
        test.insert(4);

        assertEquals("[ 20, 10, 30, 4, 15, 25, 34, 3, 5, 33, 35 ]", test.toLevelOrderString());
        assertEquals("[ 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 ]", test.toLevelOrderColor());

        test.insert(16);
        test.insert(17);

        assertEquals("[ 20, 10, 30, 4, 16, 25, 34, 3, 5, 15, 17, 33, 35 ]", test.toLevelOrderString());
        assertEquals("[ 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0 ]", test.toLevelOrderColor());

        test.insert(36);
        test.insert(37);

        assertEquals("[ 20, 10, 30, 4, 16, 25, 34, 3, 5, 15, 17, 33, 36, 35, 37 ]", test.toLevelOrderString());
        assertEquals("[ 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0 ]", test.toLevelOrderColor());

    }

    /**
     * This method checks case 3 functionality through JUnit testing. When the parent of
     * the inserted node's sibling is red, change the color of the parent, parent's sibling,
     * and parent's parent
     */
    @Test
    public void test3() {

        RedBlackTreeAE<Integer> test = new RedBlackTreeAE<>();

        test.insert(10);
        test.insert(11);
        test.insert(12);

        assertEquals("[ 11, 10, 12 ]", test.toLevelOrderString());
        assertEquals("[ 1, 0, 0 ]", test.toLevelOrderColor());

        test.insert(13);

        assertEquals("[ 11, 10, 12, 13 ]", test.toLevelOrderString());
        assertEquals("[ 1, 1, 1, 0 ]", test.toLevelOrderColor());

        test.insert(16);
        test.insert(15);
        test.insert(17);

        assertEquals("[ 11, 10, 13, 12, 16, 15, 17 ]", test.toLevelOrderString());
        assertEquals("[ 1, 1, 0, 1, 1, 0, 0 ]", test.toLevelOrderColor());

        test.insert(20);

        assertEquals("[ 13, 11, 16, 10, 12, 15, 17, 20 ]", test.toLevelOrderString());
        assertEquals("[ 1, 0, 0, 1, 1, 1, 1, 0 ]", test.toLevelOrderColor());

    }
}

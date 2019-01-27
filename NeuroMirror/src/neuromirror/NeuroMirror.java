/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuromirror;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.TransferFunctionType;

/**
 *
 * @author duemchen
 */
public class NeuroMirror {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        NeuralNetwork nn = new Perceptron(2, 2);
        DataSet t = new DataSet(2, 2);
        t.addRow(new DataSetRow(new double[]{0.1, 0.2}, new double[]{0.1, 0.2}));
        t.addRow(new DataSetRow(new double[]{1.2, 0.4}, new double[]{1.2, 0.4}));
        t.addRow(new DataSetRow(new double[]{0.2, 1.2}, new double[]{0.3, 1.3}));
        t.addRow(new DataSetRow(new double[]{0.9, 1.1}, new double[]{0.8, 1.1}));
        MultiLayerPerceptron neuralNet = new MultiLayerPerceptron(TransferFunctionType.GAUSSIAN, 1, 1, 1);
        MomentumBackpropagation learningRule = (MomentumBackpropagation) neuralNet.getLearningRule();
        learningRule.setLearningRate(0.2);
        learningRule.setMomentum(0.9);
        System.out.println("learn");
        nn.learn(t);
        System.out.println("learned");
        nn.save("nn.txt");
        nn.setInput(1, 0);
        System.out.println("calc");
        nn.calculate();
        double[] out = nn.getOutput();
        System.out.println("out " + out[0] + "  " + out[1]);
    }

}

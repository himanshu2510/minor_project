/***
 * Neuroph  http://neuroph.sourceforge.net
 * Copyright by Neuroph Project (C) 2008 
 *
 * This file is part of Neuroph framework.
 *
 * Neuroph is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Neuroph is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Neuroph. If not, see <http://www.gnu.org/licenses/>.
 */

package org.neuroph.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;

import org.neuroph.core.learning.LearningRule;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.util.NeuralNetworkType;
import org.neuroph.util.VectorParser;
import org.neuroph.util.plugins.LabelsPlugin;
import org.neuroph.util.plugins.PluginBase;


/**
 *<pre>
 * Base class for artificial neural networks. It provides generic structure and functionality
 * for the neural networks. Neural network contains a collection of neuron layers and learning rule.
 * Custom neural networks are created by deriving from this class, creating layers of interconnected network specific neurons,
 * and setting network specific learning rule.
 *</pre>
 * 
 * @see Layer
 * @see LearningRule
 * @author Zoran Sevarac <sevarac@gmail.com>
 */
public class NeuralNetwork extends Observable implements Runnable, Serializable {
	
	/**
	 * The class fingerprint that is set to indicate serialization
	 * compatibility with a previous version of the class.
	 */
	private static final long serialVersionUID = 3L;

	/**
	 * Network type id (see neuroph.util.NeuralNetworkType)
	 */
	private NeuralNetworkType type;

	/**
	 * Neural network
	 */
	private Vector<Layer> layers;

	/**
	 * Reference to network input neurons
	 */
	private Vector<Neuron> inputNeurons;

	/**
	 * Reference to newtwork output neurons
	 */
	private Vector<Neuron> outputNeurons;

	/**
	 * Learning rule for this network
	 */
	private LearningRule learningRule; // learning algorithme

	/**
	 * Separate thread for learning rule
	 */
	private transient Thread learningThread; // thread for learning rule
	
	/**
	 * Plugins collection
	 */	
	private Map<String, PluginBase> plugins;

	/**
	 * Creates an instance of empty neural network.
	 */
	public NeuralNetwork() {
		this.layers = new Vector<Layer>();
		this.plugins = new HashMap<String, PluginBase>();
		this.addPlugin(new LabelsPlugin());	
	}

	/**
	 * Adds layer to neural network
	 * 
	 * @param layer
	 *            layer to add
	 */
	public void addLayer(Layer layer) {
		layer.setParentNetwork(this);
		this.layers.add(layer);
	}

	/**
	 * Adds layer to specified index position in network
	 * 
	 * @param idx
	 *            index position to add layer
	 * @param layer
	 *            layer to add
	 */
	public void addLayer(int idx, Layer layer) {
		layer.setParentNetwork(this);
		this.layers.add(idx, layer);
	}

	/**
	 * Removes specified layer from network
	 * 
	 * @param layer
	 *            layer to remove
	 */
	public void removeLayer(Layer layer) {
		this.layers.removeElement(layer);
	}

	/**
	 * Removes layer at specified index position from net
	 * 
	 * @param idx
	 *            int value represents index postion of layer which should be
	 *            removed
	 */
	public void removeLayerAt(int idx) {
		this.layers.removeElementAt(idx);
	}

	/**
	 * Returns interface for iterating layers
	 * 
	 * @return iterator interface for network getLayersIterator
	 */
	public Iterator<Layer> getLayersIterator() {
		return this.layers.iterator();
	}

	/**
	 * Returns getLayersIterator Vector collection
	 * 
	 * @return getLayersIterator Vector collection
	 */
	public Vector<Layer> getLayers() {
		return this.layers;
	}

	/**
	 * Returns layer at specified index
	 * 
	 * @param idx
	 *            layer index position
	 * @return layer at specified index position
	 */
	public Layer getLayerAt(int idx) {
		return this.layers.elementAt(idx);
	}

	/**
	 * Returns index position of the specified layer
	 * 
	 * @param layer
	 *            requested Layer object
	 * @return layer position index
	 */
	public int indexOf(Layer layer) {
		return this.layers.indexOf(layer);
	}

	/**
	 * Returns number of layers in network
	 * 
	 * @return number of layes in net
	 */
	public int getLayersCount() {
		return this.layers.size();
	}

	/**
	 * Sets network input. Input Vector is collection of Double values.
	 * 
	 * @param inputVector
	 *            network input vector
	 */
	public void setInput(Vector<Double> inputVector) {
		if (inputVector.size() != this.inputNeurons.size())
			throw new RuntimeException(
					"Input vector size does not match network input dimension!");

		Iterator<Double> inputIterator = inputVector.iterator();
		
		for(Neuron neuron : this.inputNeurons) {
			Double input = inputIterator.next(); // get input value
			neuron.setInput(input.doubleValue()); // set input for that
		}		
	}

   	/**
	 * Sets network input. Input is array of double values.
	 *
	 * @param inputArray
	 *            network input as double array
	 */
	public void setInput(double ... inputArray) {
        if (inputArray.length != inputNeurons.size())
            throw new RuntimeException("Input vector size does not match network input dimension!");
		setInput(VectorParser.convertToVector(inputArray));
	}

	/**
	 * Returns network output Vector. Output Vector is a collection of Double
	 * values.
	 * 
	 * @return network output Vector
	 */
	public Vector<Double> getOutput() {
		Vector<Double> outputVector = new Vector<Double>();
		
		for(Neuron neuron : this.outputNeurons) {
			double output = neuron.getOutput();
			outputVector.add(new Double(output));
		}		
		
		return outputVector;
	}

	/**
	 * Returns network output vector as double array
         *
	 * @return network output vector as double array
	 */
	public double[] getOutputAsArray() {
		return VectorParser.convertToArray(getOutput());
	}

	/**
	 * Performs calculation on whole network
	 */
	public void calculate() {
		for(Layer layer : this.layers) {
			layer.calculate();
		}		
	}

	/**
	 * Resets the activation levels for whole network
	 */
	public void reset() {
		for(Layer layer : this.layers) {
			layer.reset();
		}		
	}

	/**
	 * Implementation of Runnable interface for calculating network in the
	 * separate thread.
	 */
        @Override
	public void run() {
		this.calculate();
	}

	/**
	 * Trains the network to learn the specified training set.
	 * This method is deprecated use learnInNewThread or learnInSameThread instead.
	 * @param trainingSetToLearn
	 *            set of training elements to learn
         * @deprecated
	 */
	public void learn(TrainingSet trainingSetToLearn) {
                learnInNewThread(trainingSetToLearn);
	}

        /**
         * Starts learning in a new thread to learn the specified training set,
         * and immediately returns from method to the current thread execution
         * @param trainingSetToLearn
         *              set of training elements to learn
         */
        public void learnInNewThread(TrainingSet trainingSetToLearn) {
                    learningRule.setTrainingSet(trainingSetToLearn);
                    learningThread = new Thread(learningRule);
                    learningThread.start();
        }

        /**
         * Starts learning with specified learning rule in new thread to learn the
         * specified training set, and immediately returns from method to the current thread execution
         * @param trainingSetToLearn
         *              set of training elements to learn
         * @param learningRule
         *              learning algorithm
         */
        public void learnInNewThread(TrainingSet trainingSetToLearn, LearningRule learningRule) {
            setLearningRule(learningRule);
                    learningRule.setTrainingSet(trainingSetToLearn);
            learningThread = new Thread(learningRule);
            learningThread.start();
        }

        /**
         * Starts the learning in the current running thread to learn the specified
         * training set, and returns from method when network is done learning
         * @param trainingSetToLearn
         *              set of training elements to learn
         */
        public void learnInSameThread(TrainingSet trainingSetToLearn) {
                    learningRule.setTrainingSet(trainingSetToLearn);
            learningRule.run();
        }
        /**
         * Starts the learning with specified learning rule in the current running
         * thread to learn the specified training set, and returns from method when network is done learning
         * @param trainingSetToLearn
         *              set of training elements to learn
         * @param learningRule
         *              learning algorithm
         * *
         */
        public void learnInSameThread(TrainingSet trainingSetToLearn, LearningRule learningRule) {
            setLearningRule(learningRule);
                    learningRule.setTrainingSet(trainingSetToLearn);
            learningRule.run();
        }

	/**
	 * Stops learning
	 */
	public void stopLearning() {
		learningRule.stopLearning();
	}

	/**
	 * Randomizes connection weights for the whole network
	 */
	public void randomizeWeights() {
		for(Layer layer : this.layers) {
			layer.randomizeWeights();
		}		
	}

	/**
	 * Returns type of this network
	 * 
	 * @return network type
	 */
	public NeuralNetworkType getNetworkType() {
		return type;
	}

	/**
	 * Sets type for this network
	 * 
	 * @param type network type
	 */
	public void setNetworkType(NeuralNetworkType type) {
		this.type = type;
	}

	/**
	 * Gets reference to input neurons Vector.
	 * 
	 * @return input neurons Vector
	 */
	public Vector<Neuron> getInputNeurons() {
		return this.inputNeurons;
	}

	/**
	 * Sets reference to input neurons Vector
	 * 
	 * @param inputNeurons
	 *            input neurons collection
	 */
	public void setInputNeurons(Vector<Neuron> inputNeurons) {
		this.inputNeurons = inputNeurons;
	}

	/**
	 * Returns reference to output neurons Vector.
	 * 
	 * @return output neurons Vector
	 */
	public Vector<Neuron> getOutputNeurons() {
		return this.outputNeurons;
	}

	/**
	 * Sets reference to output neurons Vector.
	 * 
	 * @param outputNeurons
	 *            output neurons collection
	 */
	public void setOutputNeurons(Vector<Neuron> outputNeurons) {
		this.outputNeurons = outputNeurons;
	}

	/**
	 * Returns the learning algorithm of this network
	 * 
	 * @return algorithm for network training
	 */
	public LearningRule getLearningRule() {
		return this.learningRule;
	}

	/**
	 * Sets learning algorithm for this network
	 * 
	 * @param learningRule learning algorithm for this network
	 */
	public void setLearningRule(LearningRule learningRule) {
        learningRule.setNeuralNetwork(this);
		this.learningRule = learningRule;
	}

	/**
	 * Notifies observers about some change
	 */
	public void notifyChange() {
		setChanged();
		notifyObservers();
		clearChanged();
	}

	/**
	 * Creates connection with specified weight value between specified neurons
	 * 
	 * @param fromNeuron neuron to connect
         * @param toNeuron neuron to connect to
	 * @param weightVal connection weight value
	 */	
	public void createConnection(Neuron fromNeuron, Neuron toNeuron, double weightVal) {
		Connection connection = new Connection(fromNeuron, weightVal);
		toNeuron.addInputConnection(connection);
	}

	
	@Override
	public String toString() {
		if (plugins.containsKey("LabelsPlugin")) {
			LabelsPlugin labelsPlugin = ((LabelsPlugin)this.getPlugin("LabelsPlugin"));
			String label = labelsPlugin.getLabel(this);
			if (label!=null) return label;
		}
		
		return super.toString();
	}

	/**
	 * Saves neural network into the specified file.
	 * 
	 * @param filePath
	 *		file path to save network into
	 */
	public void save(String filePath) {
		ObjectOutputStream out = null;
		try {
			File file = new File(filePath);
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(this);
			out.flush();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Loads neural network from the specified file.
	 * 
	 * @param filePath
	 *		file path to load network from
	 * @return loaded neural network as NeuralNetwork object
	 */	
	public static NeuralNetwork load(String filePath) {
		ObjectInputStream oistream = null;
		
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				throw new FileNotFoundException("Cannot find file: " + filePath);
			}

			oistream = new ObjectInputStream(new FileInputStream(filePath));
			NeuralNetwork nnet = (NeuralNetwork) oistream.readObject();

			return nnet;

		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		} finally {
			if(oistream != null) {
				try {
					oistream.close();
				} catch (IOException ioe) {
				}
			}
		}

		return null;
	}

	/**
	 * Adds plugin to neural network
	 * @param plugin neural network plugin to add
	 */
	public void addPlugin(PluginBase plugin) {
		plugin.setParentNetwork(this);
		this.plugins.put(plugin.getName(), plugin);
	}
	
	/**
	 * Returns the requested plugin
	 * @param pluginName name of the plugin to get
	 * @return plugin with specified name
	 */
	public PluginBase getPlugin(String pluginName) {
		return this.plugins.get(pluginName);
	}
	
	/**
	 * Removes the plugin with specified name
	 * @param pluginName name of the plugin to remove
	 */
	public void removePlugin(String pluginName) {
		this.plugins.remove(pluginName);
	}
	

}

# handwriting-recognition
Java handwritten digit/letter neural network

# Build
`mvn clean package`

# Run
`java -jar target/handwriting-recognition-1.0-SNAPSHOT.jar <1|2|3> [-epochs=<num-of-epochs>] [-cost=<q|ce>] [-eta=<learning-rate>] [-hidden=<num-of-neurons>]`  
 where  
 `<1> : MNIST digit dataset`
 `<2> : EMNIST letter dataset`
 `<3> : EMNIST balanced digit/upper/lower-case-letter dataset`
 `<4> : Fashion MNIST dataset`

# Reference
- [Neural Networks and Deep Learning by Michael Nielsen](http://neuralnetworksanddeeplearning.com/)
- [MNIST Data](http://yann.lecun.com/exdb/mnist/)
- [EMNIST Data](https://www.nist.gov/itl/products-and-services/emnist-dataset)
- [Fashion MNIST Data](https://github.com/zalandoresearch/fashion-mnist)


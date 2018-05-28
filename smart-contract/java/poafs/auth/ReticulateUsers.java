package poafs.auth;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.2.0.
 */
public class ReticulateUsers extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b5060008054600160a060020a03191633179055610a8a806100326000396000f3006080604052600436106100985763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166314ffa2c2811461009d5780632b68b9c61461016b5780633d4a068d1461018257806399a4b08f146101ef578063b7f2749414610210578063c5b76ff214610285578063de4f1a2a146102a6578063df4a91451461037b578063f0322088146103d4575b600080fd5b3480156100a957600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526100f69436949293602493928401919081908401838280828437509497506103f59650505050505050565b6040805160208082528351818301528351919283929083019185019080838360005b83811015610130578181015183820152602001610118565b50505050905090810190601f16801561015d5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561017757600080fd5b50610180610414565b005b34801561018e57600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526101db9436949293602493928401919081908401838280828437509497506104379650505050505050565b604080519115158252519081900360200190f35b3480156101fb57600080fd5b506100f6600160a060020a03600435166104bc565b34801561021c57600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526102699436949293602493928401919081908401838280828437509497506105679650505050505050565b60408051600160a060020a039092168252519081900360200190f35b34801561029157600080fd5b506100f6600160a060020a0360043516610664565b3480156102b257600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526101db94369492936024939284019190819084018382808284375050604080516020601f89358b018035918201839004830284018301909452808352979a99988101979196509182019450925082915084018382808284375050604080516020601f89358b018035918201839004830284018301909452808352979a9998810197919650918201945092508291508401838280828437509497506106d69650505050505050565b34801561038757600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526100f69436949293602493928401919081908401838280828437509497506109369650505050505050565b3480156103e057600080fd5b506100f6600160a060020a036004351661094e565b6060600061040283610567565b905061040d8161094e565b9392505050565b600054600160a060020a031633141561043557600054600160a060020a0316ff5b565b60006001826040518082805190602001908083835b6020831061046b5780518252601f19909201916020918201910161044c565b51815160209384036101000a600019018019909216911617905292019485525060405193849003019092205474010000000000000000000000000000000000000000900460ff16925050505b919050565b600160a060020a0381166000908152600260208181526040928390208201805484516001821615610100026000190190911693909304601f8101839004830284018301909452838352606093909183018282801561055b5780601f106105305761010080835404028352916020019161055b565b820191906000526020600020905b81548152906001019060200180831161053e57829003601f168201915b50505050509050919050565b60006001826040518082805190602001908083835b6020831061059b5780518252601f19909201916020918201910161057c565b51815160209384036101000a600019018019909216911617905292019485525060405193849003019092205474010000000000000000000000000000000000000000900460ff1615915061065c9050576001826040518082805190602001908083835b6020831061061d5780518252601f1990920191602091820191016105fe565b51815160209384036101000a6000190180199092169116179052920194855250604051938490030190922054600160a060020a031692506104b7915050565b5060006104b7565b600160a060020a038116600090815260026020818152604092839020805484516001821615610100026000190190911693909304601f8101839004830284018301909452838352606093909183018282801561055b5780601f106105305761010080835404028352916020019161055b565b60408051606081018252848152602080820185905281830184905233600090815260028252928320825180519192610713928492909101906109c3565b50602082810151805161072c92600185019201906109c3565b50604082015180516107489160028401916020909101906109c3565b5090505061075584610437565b15156109035733600090815260026020819052604080832090518154600193829184916000196101008389161502019091160480156107cb5780601f106107a95761010080835404028352918201916107cb565b820191906000526020600020905b8154815290600101906020018083116107b7575b505092835250506040805160209281900383018120805474ff0000000000000000000000000000000000000000191674010000000000000000000000000000000000000000951515959095029490941790935582810181523383526001828401819052905187519192889282918401908083835b6020831061085e5780518252601f19909201916020918201910161083f565b51815160209384036101000a600019018019909216911617905292019485525060405193849003810190932084518154959094015173ffffffffffffffffffffffffffffffffffffffff19909516600160a060020a039094169390931774ff00000000000000000000000000000000000000001916740100000000000000000000000000000000000000009415159490940293909317909155506001915061040d9050565b60408051602081810180845260008084523381526002909252929020905161092b92906109c3565b506000949350505050565b6060600061094383610567565b905061040d816104bc565b600160a060020a0381166000908152600260208181526040928390206001908101805485519281161561010002600019011693909304601f810183900483028201830190945283815260609390929183018282801561055b5780601f106105305761010080835404028352916020019161055b565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10610a0457805160ff1916838001178555610a31565b82800160010185558215610a31579182015b82811115610a31578251825591602001919060010190610a16565b50610a3d929150610a41565b5090565b610a5b91905b80821115610a3d5760008155600101610a47565b905600a165627a7a7230582063a681c25affc76022116754f5438a865e88e39ddddc5e40734436745fac9eca0029";

    protected ReticulateUsers(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected ReticulateUsers(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<byte[]> getPublicKeyForUserByName(String userName) {
        Function function = new Function("getPublicKeyForUserByName", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(userName)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<TransactionReceipt> destruct() {
        Function function = new Function(
                "destruct", 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> isUserNameTaken(String username) {
        Function function = new Function("isUserNameTaken", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(username)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<String> getRootDirForUser(String addr) {
        Function function = new Function("getRootDirForUser", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(addr)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> getAddressForUserName(String userName) {
        Function function = new Function("getAddressForUserName", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(userName)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> getUserNameForAddress(String addr) {
        Function function = new Function("getUserNameForAddress", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(addr)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> registerUser(String username, byte[] pubKey, String rootDir) {
        Function function = new Function(
                "registerUser", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(username), 
                new org.web3j.abi.datatypes.DynamicBytes(pubKey), 
                new org.web3j.abi.datatypes.Utf8String(rootDir)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> getRootDirForUserByName(String userName) {
        Function function = new Function("getRootDirForUserByName", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(userName)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<byte[]> getPublicKeyForUser(String addr) {
        Function function = new Function("getPublicKeyForUser", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(addr)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public static RemoteCall<ReticulateUsers> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ReticulateUsers.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<ReticulateUsers> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ReticulateUsers.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static ReticulateUsers load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new ReticulateUsers(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static ReticulateUsers load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ReticulateUsers(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }
}
